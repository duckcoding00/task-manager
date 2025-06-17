import { error, fail, type Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import type { ErrorResponse, GetProjecTaskResponse, project } from '$lib/types/task';

export const load = (async ({ fetch, params }) => {
	try {
		const response = await fetch(`http://127.0.0.1:8080/projects/${params.id}`);
		const result: GetProjecTaskResponse | ErrorResponse = await response.json();

		if (!response.ok) {
			if ('errors' in result) {
				return {
					project: null as unknown as project,
					error: result.errors
				};
			}

			throw error(response.status, 'failed load projects');
		}

		if ('data' in result) {
			return {
				project: result.data.project,
				members: result.data.members,
				tasks: result.data.tasks,
				title: result.data.project.name,
				description: result.data.project.description
			};
		}
	} catch (err) {
		console.error('Error loading projects:', err);
		throw error(500, 'Failed to projects tasks');
	}
}) satisfies PageServerLoad;

export const actions: Actions = {
	create: async ({ fetch, request, params }) => {
		const form = await request.formData();
		const title = form.get('title');
		const description = form.get('description');
		const priority = form.get('priority');
		const due_date = form.get('date');

		const body = JSON.stringify({
			title,
			description,
			priority,
			due_date
		});

		console.log(body);

		try {
			const response = await fetch(`http://127.0.0.1:8080/projects/${params.id}`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Accept: 'application/json'
				},
				body: body
			});

			const result = await response.json();

			if (!response.ok) {
				if (result.errors === 'VALIDATION_ERROR') {
					let validations: string[] = [];

					if (result.details?.validations && Array.isArray(result.details.validations)) {
						validations = result.details.validations;
					} else if (result.details?.error && typeof result.details.error === 'string') {
						validations = [result.details.error];
					} else if (result.message) {
						validations = [result.message];
					}

					return fail(400, {
						validations,
						message: validations.join(', '),
						type: 'validation_error'
					});
				}

				return fail(response.status, {
					message: result.message || result.error || 'Failed to create task',
					backendError: result.errors || 'Unknown backend error',
					type: 'backend_error'
				});
			}
		} catch (error) {
			console.log('Network or other error:', error);

			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error'
			});
		}
	},

	archive: async ({ fetch, request }) => {
		const form = await request.formData();
		const id = form.get('id') as string;
		const status = form.get('status') as string;

		const body = JSON.stringify({
			id,
			status
		});

		console.log(body);
		try {
			const response = await fetch(
				`http://127.0.0.1:8080/projects/${id}/archive/?status=${status}`,
				{
					method: 'PATCH',
					headers: {
						'Content-Type': 'application/json',
						Accept: 'application/json'
					}
				}
			);

			const result = await response.json();
			console.log(result);
			if (!response.ok) {
				return fail(response.status, {
					message: result.message || result.error || 'Failed to create task',
					backendError: result.errors || 'Unknown backend error',
					type: 'backend_error'
				});
			}
		} catch (err) {
			console.log('Network or other error:', err);

			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error'
			});
		}
	}
};
