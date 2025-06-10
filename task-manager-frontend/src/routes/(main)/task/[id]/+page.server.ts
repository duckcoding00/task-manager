import type { SingleTaskResponse, TaskErrorResponse } from '$lib/types/task';
import { error } from '@sveltejs/kit';
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
