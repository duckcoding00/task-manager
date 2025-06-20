import type { ErrorResponse, SingleTaskResponse } from '$lib/types/task';
import { error, fail, isRedirect, redirect, type Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load = (async ({ fetch, params }) => {
	try {
		const response = await fetch(
			`http://127.0.0.1:8080/projects/${params.id}/task/${params.taskID}`
		);
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

		const subTaskResponse = await fetch(
			`http://127.0.0.1:8080/projects/${params.id}/task/${params.taskID}/subtask`
		);
		const subTaskResult = await subTaskResponse.json();

		if (!subTaskResponse.ok) {
			if (
				'errors' in subTaskResult &&
				subTaskResult.errors === 'NOT_FOUND' &&
				subTaskResult.status_code === 404
			) {
				throw error(404, subTaskResult.details.error);
			}

			if ('errors' in subTaskResult) {
				throw error(subTaskResponse.status, subTaskResult.errors);
			}

			throw error(subTaskResponse.status, 'Failed to load task');
		}

		if ('data' in result) {
			return {
				task: result.data,
				subtask: subTaskResult.data,
				error: null,
				title: result.data.title,
				description: result.data.description
			};
		}

		throw error(500, 'Invalid data format');
	} catch (err) {
		console.error('Error loading task:', err);
		if (err && typeof err === 'object' && 'status' in err) {
			throw err;
		}
		throw error(500, 'Failed to load task');
	}
}) satisfies PageServerLoad;

export const actions: Actions = {
	updateStatus: async ({ request, fetch, params }) => {
		const form = await request.formData();
		const taskId = form.get('taskId') as string;
		const status = form.get('status') as string;

		console.log(status);
		console.log(taskId);

		try {
			const response = await fetch(
				`http://127.0.0.1:8080/projects/${params.id}/task/${params.taskID}/?status=${status}`,
				{
					method: 'PATCH',
					headers: {
						'Content-Type': 'application/json',
						Accept: 'application/json'
					}
				}
			);

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

	updatePriority: async ({ request, fetch, params }) => {
		const form = await request.formData();
		const taskId = form.get('taskId') as string;
		// const projectId = form.get('projectId') as string;
		const priority = form.get('priority') as string;

		console.log(priority);
		console.log(taskId);

		try {
			const response = await fetch(
				`http://127.0.0.1:8080/projects/${params.id}/task/${params.taskID}/?priority=${priority}`,
				{
					method: 'PATCH',
					headers: {
						'Content-Type': 'application/json',
						Accept: 'application/json'
					}
				}
			);

			const result = await response.json();
			console.log('Update priority result:', result);

			if (!response.ok) {
				return fail(response.status || 400, {
					message: result.message || result.error || 'Update priority failed',
					backendError: result.errors || 'Unknown error'
				});
			}

			return {
				success: true,
				message: 'Status priority successfully'
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

	delete: async ({ request, fetch, params }) => {
		const form = await request.formData();
		const id = form.get('id') as string;
		console.log(id);

		try {
			const response = await fetch(
				`http://127.0.0.1:8080/projects/${params.id}/task/${params.taskID}`,
				{
					method: 'DELETE',
					headers: {
						'Content-Type': 'application/json',
						Accept: 'application/json'
					}
				}
			);

			const result = await response.json();
			console.log(result);
			if (!response.ok) {
				return fail(response.status || 400, {
					message: result.message || result.error || 'Insert failed',
					backendError: result.errors || 'Unknown error'
				});
			}

			throw redirect(303, `/teams/${params.id}`);
		} catch (error) {
			if (isRedirect(error)) throw error;
			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error'
			});
		}
	},

	updateTask: async ({ request, fetch, params }) => {
		const form = await request.formData();
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
			const response = await fetch(
				`http://127.0.0.1:8080/projects/${params.id}/task/${params.taskID}`,
				{
					method: 'PUT',
					headers: {
						'Content-Type': 'application/json',
						Accept: 'application/json'
					},
					body: body
				}
			);

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
	},

	subtask: async ({ request, fetch, params }) => {
		const form = await request.formData();
		const task = form.get('subtask') as string;
		const body = JSON.stringify({
			task
		});
		try {
			const response = await fetch(
				`http://127.0.0.1:8080/projects/${params.id}/task/${params.taskID}/subtask`,
				{
					method: 'POST',
					headers: {
						'Content-Type': 'application/json',
						Accept: 'application/json'
					},
					body
				}
			);

			const result = await response.json();
			console.log('insert subtask result:', result);

			if (!response.ok) {
				return fail(response.status || 400, {
					message: result.message || result.error || 'insert subtask failed',
					backendError: result.errors || 'Unknown error'
				});
			}

			return {
				success: true,
				message: 'insert subtask successfully'
			};
		} catch (error) {
			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error'
			});
		}
	},

	done: async ({ request, fetch, params }) => {
		const form = await request.formData();
		const taskID = form.get('subtask') as string;

		try {
			const response = await fetch(
				`http://127.0.0.1:8080/projects/${params.id}/task/${params.taskID}/subtask/${taskID}`,
				{
					method: 'PATCH',
					headers: {
						'Content-Type': 'application/json',
						Accept: 'application/json'
					}
				}
			);

			const result = await response.json();
			console.log('insert subtask result:', result);

			if (!response.ok) {
				return fail(response.status || 400, {
					message: result.message || result.error || 'insert subtask failed',
					backendError: result.errors || 'Unknown error'
				});
			}

			return {
				success: true,
				message: 'insert subtask successfully'
			};
		} catch (error) {
			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error'
			});
		}
	},

	deleteSubtask: async ({ request, fetch, params }) => {
		const form = await request.formData();
		const taskID = form.get('subtask') as string;
		console.log('task id', taskID);
		try {
			const response = await fetch(
				`http://127.0.0.1:8080/projects/${params.id}/task/${params.taskID}/subtask/${taskID}`,
				{
					method: 'DELETE',
					headers: {
						'Content-Type': 'application/json',
						Accept: 'application/json'
					}
				}
			);

			const result = await response.json();
			console.log('insert subtask result:', result);

			if (!response.ok) {
				return fail(response.status || 400, {
					message: result.message || result.error || 'insert subtask failed',
					backendError: result.errors || 'Unknown error'
				});
			}

			return {
				success: true,
				message: 'insert subtask successfully'
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
