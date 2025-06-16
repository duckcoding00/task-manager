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
				if (result.errors === 'VALIDATION_ERROR' && result.details?.validations) {
					const validations: string[] = result.details?.validations;

					return fail(400, {
						validations,
						message: validations.join(', ')
					});
				}

				return fail(response.status || 400, {
					message: result.message || result.error || 'Insert failed',
					backendError: result.errors || 'Unknown error'
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
	}
};
