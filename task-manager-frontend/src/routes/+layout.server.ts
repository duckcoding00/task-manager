import type { LayoutServerLoad } from './$types';

export const load = (async ({ cookies }) => {
	const token = cookies.get('access_token');
	let isAuth = false;

	if (token && token.trim() !== '') {
		isAuth = true;
	}

	return {
		isAuth
	};
}) satisfies LayoutServerLoad;
