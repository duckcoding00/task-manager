import { redirect } from '@sveltejs/kit';
import type { Actions } from './$types';

export const actions: Actions = {
	logout: ({ cookies }) => {
		cookies.delete('access_token', {
			path: '/'
		});

		throw redirect(303, '/');
	}
};
