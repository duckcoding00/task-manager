import type { Actions } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';
import { fail, isRedirect, redirect } from '@sveltejs/kit';

export const load = (async () => {
	return {};
}) satisfies PageServerLoad;

export const actions: Actions = {
	login: async ({ request, fetch, cookies, url }) => {
		const form = await request.formData();

		const username = form.get('username');
		const password = form.get('password');
		const path = url.searchParams.get('redirectTo') || '';

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

			const contentType = response.headers.get('content-type');
			const contentLength = response.headers.get('content-length');

			console.log('Content-Type:', contentType);
			console.log('Content-Length:', contentLength);

			let result;

			if (contentLength === '0' || !contentType?.includes('application/json')) {
				console.log('Empty or non-JSON response received');
				result = {
					errors: response.ok ? 'SUCCESS' : 'EMPTY_RESPONSE',
					message: response.ok ? 'Login successful' : 'Server returned empty response'
				};
			} else {
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

				const accessToken = result.data?.access_token || result.access_token || result.token;

				if (accessToken) {
					// prod
					// cookies.set('access_token', accessToken, {
					// 	path: '/',
					// 	httpOnly: true,
					// 	sameSite: 'strict',
					// 	maxAge: 60 * 60 * 24 * 7
					// });

					// dev
					cookies.set('access_token', accessToken, {
						path: '/',
						httpOnly: true,
						sameSite: 'lax',
						secure: false,
						maxAge: 60 * 60 * 24 * 7
					});
				}

				throw redirect(303, path === '/login' ? '/' : path);
			} else {
				console.log('Login failed:', result);

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
	}
};
