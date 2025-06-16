import type { ErrorResponse, GetProjectsResponse, projects } from '$lib/types/task';
import { error, fail, type Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load = (async ({ fetch }) => {
	try {
		const response = await fetch('http://127.0.0.1:8080/projects');
		const result: GetProjectsResponse | ErrorResponse = await response.json();
		console.log(result);
		if (!response.ok) {
			if ('errors' in result) {
				return {
					projects: [] as projects[],
					error: result.errors
				};
			}

			throw error(response.status, 'failed load projects');
		}

		if ('data' in result) {
			return {
				projects: result.data,
				errors: null,
				title: 'Team Projects',
				description:
					"Manage and collaborate on team projects. View project status, assign tasks, and track progress across all your team's active projects."
			};
		}
	} catch (err) {
		console.error('Error loading projects:', err);
		throw error(500, 'Failed to projects tasks');
	}
}) satisfies PageServerLoad;

export const actions: Actions = {
	create: async ({ fetch, request }) => {
		const form = await request.formData();
		const name = form.get('name');
		const description = form.get('description');
		const start_date = form.get('start_date');
		const end_date = form.get('end_date');

		const body = JSON.stringify({
			name: name,
			description: description,
			start_date: start_date,
			end_date: end_date
		});

		console.log(body);

		try {
			const response = await fetch('http://127.0.0.1:8080/projects', {
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
