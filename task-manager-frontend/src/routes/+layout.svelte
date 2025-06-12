<script lang="ts">
	import { page } from '$app/state';
	import '../app.css';

	import type { Snippet } from 'svelte';

	let { children }: { children: Snippet } = $props();
	const description: string =
		'A comprehensive task management application designed to help you organize, track, and complete your daily tasks efficiently.';
</script>

<svelte:head>
	<title>
		{page.data.title ? `${page.data.title} | Task Manager` : 'Task Manager'}
	</title>
	<meta name="description" content={page.data.description ? page.data.description : description} />
</svelte:head>

{@render children()}
