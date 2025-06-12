<script lang="ts">
	import { Section, Page404 } from 'flowbite-svelte-blocks';
	import { Button } from 'flowbite-svelte';
	import { page } from '$app/state';

	let path = $derived(page.url.pathname);
	
	// Function untuk check apakah path adalah task route yang valid
	function isTaskRoute(pathname: string): boolean {
		return pathname === '/task' || pathname.startsWith('/task/');
	}
</script>

{#if page.status === 404}
	<Section name="page404">
		<Page404>
			{#snippet h1()}
				<span class="text-red-600 dark:text-red-400">{page.status}</span>
			{/snippet}
			{#snippet paragraph()}
				<p class="mb-4 text-3xl font-bold tracking-tight text-gray-900 md:text-4xl dark:text-white">
					Not Found
				</p>
				<p class="mb-4 text-lg font-light text-gray-500 dark:text-gray-400">
					{page.error?.message
						? page.error.message
						: `Sorry, we can't find that page. You'll find lots to explore on the home page.`}
				</p>
				{#if isTaskRoute(path)}
					<Button href="/task" size="lg" color="gray">Back to Tasks</Button>
				{:else}
					<Button href="/" size="lg" color="gray">Back to Home</Button>
				{/if}
			{/snippet}
		</Page404>
	</Section>
{/if}
