import type { HandleFetch } from '@sveltejs/kit';

export const handleFetch: HandleFetch = async ({ event, request, fetch }) => {
	const cookie = event.cookies.get('access_token');

	if (request.url.startsWith('http://127.0.0.1:8080/')) {
		if (cookie) {
			request.headers.set('Authorization', `Bearer ${cookie}`);
		}

		if (!request.headers.get('Content-Type') && request.method !== 'GET') {
			request.headers.set('Content-Type', 'application/json');
		}
	}

	try {
		const response = await fetch(request);

		if (response.status === 401 && cookie) {
			event.cookies.delete('access_token', { path: '/' });
		}

		return response;
	} catch (error) {
		console.error('Fetch error:', error);
		throw error;
	}
};
