import { redirect } from '@sveltejs/kit';
import type { Actions } from './$types';
import type { PageServerLoad } from './$types';

export const load = (async () => {
	return {
		title: 'Home',
		description:
			'Welcome to your personal task management hub. View, organize, and track your tasks efficiently. Create new projects, set priorities, and manage deadlines all in one centralized dashboard.'
	};
}) satisfies PageServerLoad;

export const actions: Actions = {
	logout: ({ cookies }) => {
		cookies.delete('access_token', {
			path: '/'
		});

		throw redirect(303, '/');
	}
};
