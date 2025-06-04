import type { Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { fail, redirect } from '@sveltejs/kit';

export const load = (async () => {
	return {};
}) satisfies PageServerLoad;

export const actions: Actions = {
	login: async ({ request, fetch, cookies }) => {
		const form = await request.formData();

		const username = form.get('username');
		const password = form.get('password');

		const body = JSON.stringify({
			username: username?.toString(),
			password: password?.toString()
		});

		try {
			const response = await fetch('http://localhost:8080/user/login', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: body
			});

			const result = await response.json();

			if (response.ok) {
				if (result.data?.access_token) {
					cookies.set('access_token', result.data.access_token, {
						path: '/',
						httpOnly: true,
						sameSite: 'strict',
						maxAge: 60 * 60 * 24 * 7
					});
				}

				throw redirect(303, '/');
			} else {
				if (result.errors === 'VALIDATION_ERROR') {
					const validations: string[] = result.details.validations;

					const usernameErrs = validations.filter((e) => e.toLowerCase().includes('username'));
					const passwordErrs = validations.filter((e) => e.toLowerCase().includes('password'));

					return fail(400, {
						usernameErrs: usernameErrs.length > 0 ? usernameErrs : undefined,
						passwordErrs: passwordErrs.length > 0 ? passwordErrs : undefined,
						validations,
						username: passwordErrs.length > 0 ? username : undefined,
						password: usernameErrs.length > 0 ? password : undefined
					});
				}

				if (result.errors === 'FAILED_LOGIN') {
					return fail(404, {
						userExist: false,
						message: 'Login failed - user not found or invalid credentials'
					});
				}

				return fail(result.status_code || 400, {
					message: result.message || 'Login failed',
					backendError: result.errors
				});
			}
		} catch (error) {
			console.error('Login error:', error);

			if (error instanceof Response) {
				throw error;
			}

			return fail(500, {
				type: 'network_error',
				message: 'Tidak dapat terhubung ke server',
				error: error instanceof Error ? error.message : 'Unknown error'
			});
		}
	}
};
