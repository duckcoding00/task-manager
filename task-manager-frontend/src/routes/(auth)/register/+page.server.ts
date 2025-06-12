import { fail, isRedirect, redirect, type Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load = (async () => {
	return {
		title: 'Register Page',
		description:
			'Register new task manager account. Enter your username and password to sign up and manage your tasks, projects, and deadlines.'
	};
}) satisfies PageServerLoad;

export const actions: Actions = {
	register: async ({ request, fetch }) => {
		const form = await request.formData();
		const username = form.get('username') as string;
		const password = form.get('password') as string;
		const body = JSON.stringify({
			username,
			password
		});
		try {
			const response = await fetch('http://127.0.0.1:8080/user/register', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: body
			});
			const result = await response.json();
			if (!response.ok) {
				if (result.errors === 'VALIDATION_ERROR' && result.details?.validations) {
					const validations: string[] = result.details.validations;

					const usernameErrs = validations.filter((e) => e.toLowerCase().includes('username'));
					const passwordErrs = validations.filter((e) => e.toLowerCase().includes('password'));

					return fail(400, {
						usernameErrs: usernameErrs.length > 0 ? usernameErrs : undefined,
						passwordErrs: passwordErrs.length > 0 ? passwordErrs : undefined,
						validations,
						username: passwordErrs.length > 0 ? username : undefined,
						password: usernameErrs.length > 0 ? password : undefined,
						message: validations.join(', ')
					});
				}

				return fail(response.status || 400, {
					message: result.message || result.error || 'Login failed',
					backendError: result.errors || 'Unknown error',
					username: username
				});
			}

			throw redirect(303, '/login');
		} catch (error) {
			if (isRedirect(error)) throw error;

			console.log('Network or other error:', error);

			return fail(500, {
				type: 'network_error',
				message: 'Cannot connect to server. Please check your connection.',
				error: error instanceof Error ? error.message : 'Unknown network error',
				username: username?.toString()
			});
		}
	}
};
