<script lang="ts">
	import { Card } from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { getStatusClass, getStatusLabel, getStatusStyle } from '$lib/utils/label';

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
				<Card
					class="h-60 w-full max-w-sm  transition-all duration-300 hover:scale-105 hover:shadow-2xl"
				>
					<div class="flex h-full flex-col p-4">
						<div class="mb-4 flex-none">
							<h1
								class="line-clamp-2 text-xl leading-tight font-bold text-black transition-colors duration-200 hover:text-blue-300"
							>
								<a
									href="/task/{task.id}"
									class="hover:underline"
									data-sveltekit-preload-data="hover">{task.title}</a
								>
							</h1>
						</div>

						<div class="mb-4 flex-1">
							<p class="line-clamp-4 text-sm leading-relaxed text-black">
								{task.description}
							</p>
						</div>

						<div class="flex-none">
							<div class="flex items-center justify-between">
								<span
									class="rounded-full border px-3 py-1 text-xs font-semibold {getStatusStyle(
										task.status
									)}"
								>
									{getStatusLabel(task.status)}
								</span>

								<time
									class="rounded-md bg-slate-700/50 px-2 py-1 text-xs font-medium text-white"
									datetime={task.due_dated_at}
								>
									📅 {formatDate(task.due_dated_at)}
								</time>
							</div>
						</div>
					</div>
				</Card>
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
