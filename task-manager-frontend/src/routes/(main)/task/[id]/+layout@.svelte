<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { LayoutData } from './$types';
	import { Breadcrumb, BreadcrumbItem } from 'flowbite-svelte';
	import { page } from '$app/state';

	let { data, children }: { data: LayoutData; children: Snippet } = $props();
	console.log(data);
</script>

<div class="container m-5 p-5 md:m-4 md:p-4">
	<Breadcrumb aria-label="Solid background breadcrumb example" class="mb-6 md:mb-4">
		<BreadcrumbItem href="/task" home>Tasks</BreadcrumbItem>

		{#if page.url.pathname.startsWith('/task/') && page.url.pathname !== '/task'}
			<BreadcrumbItem href="#">{page.data.title || 'Task Detail'}</BreadcrumbItem>
		{/if}
	</Breadcrumb>
	{@render children()}
</div>
