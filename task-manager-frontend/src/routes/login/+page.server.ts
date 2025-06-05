import type { Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { fail, isRedirect, redirect } from '@sveltejs/kit';
import { invalidateAll } from '$app/navigation';

export const load = (async () => {
	return {};
}) satisfies PageServerLoad;

export const actions: Actions = {
	login: async ({ request, fetch, cookies, url }) => {
		const form = await request.formData();

		const username = form.get('username');
		const password = form.get('password');
		const path = url.searchParams.get('redirectTo') || '';

		// Validasi input di frontend
		if (!username || !password) {
			return fail(400, {
				message: 'Username and password are required',
				username: username?.toString(),
				validations: ['Username and password cannot be empty']
			});
		}

		const body = JSON.stringify({
			username: username.toString(),
			password: password.toString()
		});

		try {
			const response = await fetch('http://127.0.0.1:8080/user/login', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					Accept: 'application/json'
				},
				body: body
			});

			console.log('Response status:', response.status);
			console.log('Response headers:', Object.fromEntries(response.headers.entries()));

			// Cek apakah response memiliki content
			const contentType = response.headers.get('content-type');
			const contentLength = response.headers.get('content-length');

			console.log('Content-Type:', contentType);
			console.log('Content-Length:', contentLength);

			let result;

			// Handle empty response atau non-JSON content
			if (contentLength === '0' || !contentType?.includes('application/json')) {
				console.log('Empty or non-JSON response received');
				result = {
					errors: response.ok ? 'SUCCESS' : 'EMPTY_RESPONSE',
					message: response.ok ? 'Login successful' : 'Server returned empty response'
				};
			} else {
				// Baca response text terlebih dahulu untuk debugging
				const responseText = await response.text();
				console.log('Raw response:', responseText);

				if (!responseText.trim()) {
					console.log('Response is empty');
					result = {
						errors: response.ok ? 'SUCCESS' : 'EMPTY_RESPONSE',
						message: response.ok ? 'Login successful' : 'Server returned empty response'
					};
				} else {
					try {
						result = JSON.parse(responseText);
					} catch (parseError) {
						console.log('JSON parse error:', parseError);
						console.log('Response text:', responseText);
						return fail(500, {
							message: 'Invalid response from server',
							error: 'Invalid JSON response'
						});
					}
				}
			}

			console.log('Parsed result:', result);

			if (response.ok) {
				console.log('Login successful, setting cookies and redirecting...');

				// Handle different success response formats
				const accessToken = result.data?.access_token || result.access_token || result.token;

				if (accessToken) {
					cookies.set('access_token', accessToken, {
						path: '/',
						httpOnly: true,
						sameSite: 'strict',
						maxAge: 60 * 60 * 24 * 7
					});
				}

				throw redirect(303, path === '/login' ? '/' : path);
			} else {
				console.log('Login failed:', result);

				// Handle berbagai format error dari backend
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

				if (
					result.errors === 'FAILED_LOGIN' ||
					response.status === 401 ||
					response.status === 404
				) {
					return fail(401, {
						userExist: false,
						message: 'Login failed - user not found or invalid credentials',
						username: username?.toString()
					});
				}

				// Generic error handling
				return fail(response.status || 400, {
					message: result.message || result.error || 'Login failed',
					backendError: result.errors || 'Unknown error',
					username: username?.toString()
				});
			}
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
	},

	logout: ({ cookies, url }) => {
		cookies.delete('access_token', {
			path: '/'
		});

		const path = url.searchParams.get('redirectTo') || '/';

		throw redirect(303, path);
	}
};
