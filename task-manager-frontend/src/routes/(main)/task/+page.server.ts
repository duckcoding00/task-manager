import type { Task, TaskErrorResponse, TaskResponse } from '$lib/types/task';
import { error, fail, type Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load = (async ({ fetch }) => {
	try {
		const response = await fetch('http://127.0.0.1:8080/tasks');
		const result: TaskResponse | TaskErrorResponse = await response.json();

		if (!response.ok) {
			if ('errors' in result) {
				return {
					tasks: [] as Task[],
					error: result.errors
				};
			}

			throw error(response.status, 'failed to load tasks');
		}

		if ('data' in result) {
			return {
				tasks: result.data,
				errors: null,
				title: 'Tasks',
				description: 'Create and manage your daily tasks with ease'
			};
		}

		throw error(500, 'invalid data format');
	} catch (err) {
		console.error('Error loading tasks:', err);
		throw error(500, 'Failed to load tasks');
	}
}) satisfies PageServerLoad;

export const actions: Actions = {
	create: async ({ request, fetch }) => {
		const form = await request.formData();

		const title = form.get('title') as string;
		const description = form.get('description') as string;
		//const due_date_at = form.get('due-date') as string;
		const date = form.get('date') as string;

		console.log(JSON.stringify({ date: date }));

		const body = JSON.stringify({
			title: title,
			description: description,
			due_date: date
		});

		console.log(body);
		try {
			const response = await fetch('http://127.0.0.1:8080/tasks', {
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
					backendError: result.errors || 'Unknown error',
					title: title
				});
			}

			//throw redirect(303, path === '/task' ? '/task' : '/task');
		} catch (error) {
			//if (isRedirect(error)) throw error;
			console.log('Network or other error:', error);

			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error',
				username: title?.toString()
			});
		}
	}
};
