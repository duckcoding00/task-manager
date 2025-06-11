import type { SingleTaskResponse, TaskErrorResponse } from '$lib/types/task';
import { error, fail, isRedirect, redirect, type Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load = (async ({ params, fetch }) => {
	try {
		const response = await fetch(`http://127.0.0.1:8080/tasks/${params.id}`);
		const result: SingleTaskResponse | TaskErrorResponse = await response.json();

		console.log(result);
		if (!response.ok) {
			if ('errors' in result) {
				return {
					tasks: null,
					error: result.errors
				};
			}

			throw error(response.status, 'failed to load tasks');
		}

		if ('data' in result) {
			return {
				task: result.data,
				errors: null
			};
		}

		throw error(500, 'invalid data format');
	} catch (err) {
		console.error('Error loading tasks:', err);
		throw error(500, 'Failed to load tasks');
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
	}
};
