<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { LayoutData } from './$types';
	import { Breadcrumb, BreadcrumbItem } from 'flowbite-svelte';

	let { data, children }: { data: LayoutData; children: Snippet } = $props();
</script>

<div class="p-auto container m-auto md:m-4 md:p-4">
	{@render children()}
</div>
