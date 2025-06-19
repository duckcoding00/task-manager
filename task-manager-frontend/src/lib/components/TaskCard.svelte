<script lang="ts">
	import { formatDate } from '$lib/utils/formatDate';
	import { Card } from 'flowbite-svelte';
	import type { Snippet } from 'svelte';

	let isHover = $state(false);

	interface props {
		title?: string;
		date: string;
		// top right content
		tRcontent?: Snippet<[boolean]>;

		// bottom right content
		bRcontent?: Snippet;

		// bottom left content
		bLcontent?: Snippet;
	}

	let { title, date, tRcontent, bRcontent, bLcontent }: props = $props();
</script>

<Card
	class="h-55 w-60 rounded-2xl shadow-sm transition-all duration-300 hover:translate-y-1 hover:shadow-md"
	onmouseenter={() => {
		isHover = true;
	}}
	onmouseleave={() => {
		isHover = false;
	}}
	><div class="flex h-full min-h-[200px] flex-col p-4">
		<div class="flex-grow">
			<div class="flex items-center justify-between">
				<p class="text-2xl font-medium">{title}</p>
				{#if tRcontent}
					{@render tRcontent(isHover)}
				{/if}
			</div>
			<time datetime={date} class="text-sm text-gray-400">{formatDate(date)}</time>
		</div>

		<div class="mt-auto pt-4">
			<div class="flex justify-end gap-1">
				{#if bLcontent}
					{@render bLcontent()}
				{/if}
				{#if bRcontent}
					{@render bRcontent()}
				{/if}
			</div>
		</div>
	</div>
</Card>
