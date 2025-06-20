<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { LayoutData } from './$types';
	import { Breadcrumb, BreadcrumbItem } from 'flowbite-svelte';
	import { page } from '$app/state';

	let { data, children }: { data: LayoutData; children: Snippet } = $props();

	let crumbs: Array<{ label: string; href: string }> = [];

	$effect(() => {
		const tokens = page.url.pathname.split('/').filter((t) => t !== '');

		let tokenPath = '';
		crumbs = tokens.map((t) => {
			tokenPath += '/' + t;
			t = t.charAt(0).toUpperCase() + t.slice(1);
			return {
				label: page.data.label || t,
				href: tokenPath
			};
		});

		crumbs.unshift({ label: 'teams', href: '/teams' });
	});
</script>

<div class="container m-5 p-5 md:m-4 md:p-4">
	<Breadcrumb aria-label="Solid background breadcrumb example" class="mb-6 md:mb-4">
		<BreadcrumbItem href="/teams" home>Teams</BreadcrumbItem>
		<BreadcrumbItem href="#">{page.data.title}</BreadcrumbItem>
	</Breadcrumb>
	{@render children()}
</div>
