import type { LayoutServerLoad } from './$types';

export const load = (async ({ cookies }) => {
	const token = cookies.get('access_token');
	const username = cookies.get('username');
	let isAuth = false;

	if (token && token.trim() !== '') {
		isAuth = true;
	}

	return {
		isAuth,
		username
	};
}) satisfies LayoutServerLoad;
