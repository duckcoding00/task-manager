<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { LayoutData } from './$types';
	import { Breadcrumb, BreadcrumbItem, Button } from 'flowbite-svelte';

	import { browser } from '$app/environment';
	import { ArrowLeftOutline } from 'flowbite-svelte-icons';

	let { data, children }: { data: LayoutData; children: Snippet } = $props();
	function goBack() {
		if (browser) {
			history.back();
		}
	}
</script>

<div class="container m-5 p-5 md:m-4 md:p-4">
	<div class="mb-4 flex items-center gap-2">
		<Button color="light" size="sm" onclick={goBack}>
			<ArrowLeftOutline class="mr-2 h-4 w-4" />
			Back
		</Button>
	</div>

	{@render children()}
</div>
