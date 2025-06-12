import type { ErrorResponse, SingleTaskResponse } from '$lib/types/task';
import { error, fail, isRedirect, redirect, type Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load = (async ({ params, fetch }) => {
	try {
		const response = await fetch(`http://127.0.0.1:8080/tasks/${params.id}`);
		const result: SingleTaskResponse | ErrorResponse = await response.json();

		console.log(result);

		if (!response.ok) {
			if ('errors' in result && result.errors === 'NOT_FOUND' && result.status_code === 404) {
				throw error(404, result.details.error);
			}

			if ('errors' in result) {
				throw error(response.status, result.errors);
			}

			throw error(response.status, 'Failed to load task');
		}

		if ('data' in result) {
			return {
				task: result.data,
				error: null,
				title: result.data.title,
				description: result.data.description
			};
		}

		throw error(500, 'Invalid data format');
	} catch (err) {
		console.error('Error loading task:', err);
		// Jika err sudah berupa SvelteKit error, re-throw
		if (err && typeof err === 'object' && 'status' in err) {
			throw err;
		}
		throw error(500, 'Failed to load task');
	}
}) satisfies PageServerLoad;

export const actions: Actions = {
	delete: async ({ request, fetch }) => {
		const form = await request.formData();
		const id = form.get('id') as string;

		try {
			const response = await fetch(`http://127.0.0.1:8080/tasks/${id}`, {
				method: 'DELETE',
				headers: {
					'Content-Type': 'application/json',
					Accept: 'application/json'
				}
			});

			const result = await response.json();
			console.log(result);
			if (!response.ok) {
				return fail(response.status || 400, {
					message: result.message || result.error || 'Insert failed',
					backendError: result.errors || 'Unknown error'
				});
			}

			throw redirect(303, '/task');
		} catch (error) {
			if (isRedirect(error)) throw error;

			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error'
			});
		}
	},

	updateStatus: async ({ request, fetch }) => {
		const form = await request.formData();
		const taskId = form.get('taskId') as string;
		const status = form.get('status') as string;

		console.log(status);
		console.log(taskId);

		try {
			const response = await fetch(`http://127.0.0.1:8080/tasks/${taskId}?status=${status}`, {
				method: 'PATCH',
				headers: {
					'Content-Type': 'application/json',
					Accept: 'application/json'
				}
			});

			const result = await response.json();
			console.log('Update status result:', result);

			if (!response.ok) {
				return fail(response.status || 400, {
					message: result.message || result.error || 'Update status failed',
					backendError: result.errors || 'Unknown error'
				});
			}

			return {
				success: true,
				message: 'Status updated successfully'
			};
		} catch (error) {
			if (isRedirect(error)) throw error;

			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error'
			});
		}
	},

	updateTask: async ({ request, fetch }) => {
		const form = await request.formData();
		const taskId = form.get('taskId') as string;
		const title = form.get('title') as string;
		const description = form.get('description') as string;
		const due_date = form.get('date') as string;

		const body = JSON.stringify({
			title: title,
			description: description,
			due_date: due_date
		});

		console.log(body);

		try {
			const response = await fetch(`http://127.0.0.1:8080/tasks/${taskId}/update`, {
				method: 'PATCH',
				headers: {
					'Content-Type': 'application/json',
					Accept: 'application/json'
				},
				body: body
			});

			const result = await response.json();
			console.log('Update status result:', result);

			if (!response.ok) {
				return fail(response.status || 400, {
					message: result.message || result.error || 'Update task failed',
					backendError: result.errors || 'Unknown error'
				});
			}

			return {
				success: true,
				message: 'Status updated successfully'
			};
		} catch (error) {
			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error'
			});
		}
	}
};
