<script lang="ts">
	import { Card } from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { getStatusClass, getStatusLabel, getStatusStyle } from '$lib/utils/label';
	import TaskCard from '$lib/components/TaskCard.svelte';
	import { AngleRightOutline } from 'flowbite-svelte-icons';
	import { goto, preloadData } from '$app/navigation';

	let { data }: { data: PageData } = $props();

	let tasks = $state(data.tasks);
	console.log(tasks);

	function formatDate(dateString: string): string {
		return new Date(dateString).toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			hour: 'numeric',
			minute: '2-digit',
			hour12: true
		});
	}
</script>

<div class="m-4 p-4">
	{#if tasks && tasks.length > 0}
		<div
			class="flex flex-col items-center gap-6 md:grid md:grid-cols-2 md:items-stretch lg:grid-cols-3 xl:grid-cols-4"
		>
			{#each tasks as task (task.id)}
				<TaskCard title={task.title} date={task.due_dated_at}>
					{#snippet tRcontent(hovered: boolean)}
						{#if hovered}
							<AngleRightOutline
								class="cursor-pointer"
								size="md"
								onclick={() => {
									preloadData(`/task/${task.id}`);
									goto(`/task/${task.id}`);
								}}
							/>
						{/if}
					{/snippet}
					{#snippet bRcontent()}
						<span
							class="rounded-full border px-3 py-1 text-xs font-semibold {getStatusStyle(
								task.status
							)}"
						>
							{getStatusLabel(task.status)}
						</span>
					{/snippet}
				</TaskCard>
			{/each}
		</div>
	{:else}
		<div class="py-16 text-center">
			<div class="mx-auto mb-4 flex h-24 w-24 items-center justify-center rounded-full bg-gray-100">
				<svg class="h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
					></path>
				</svg>
			</div>
			<h2 class="mb-2 text-2xl font-bold text-gray-600">No Tasks Available</h2>
			<p class="text-gray-500">Create your first task to get started!</p>
		</div>
	{/if}
</div>

<style>
	.line-clamp-2 {
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}

	.line-clamp-4 {
		display: -webkit-box;
		-webkit-line-clamp: 4;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}
</style>
