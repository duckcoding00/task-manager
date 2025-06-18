<script lang="ts">
	import { Button, Card } from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { formatDate } from '$lib/utils/formatDate';

	let { data }: { data: PageData } = $props();
	let projects = $state(data.projects);

	function getStatusClass(status: string) {
		switch (status) {
			case 'active':
				return 'bg-green-100 text-green-800 border border-green-200';
			case 'inactive':
				return 'bg-gray-100 text-gray-800 border border-gray-200';
			case 'archive':
				return 'bg-blue-100 text-blue-800 border border-blue-200';
			case 'cancel':
				return 'bg-red-100 text-red-800 border border-red-200';
			default:
				return 'bg-gray-100 text-gray-800 border border-gray-200';
		}
	}

	function getStatusText(status: string) {
		switch (status) {
			case 'active':
				return 'Active';
			case 'inactive':
				return 'Inactive';
			case 'archive':
				return 'Archived';
			case 'cancel':
				return 'Cancelled';
			default:
				return status.charAt(0).toUpperCase() + status.slice(1);
		}
	}
</script>

<div class="container mx-auto px-4 py-8">
	{#if projects && projects.length > 0}
		<div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
			{#each projects as project (project.id)}
				<Card
					class="group h-full border-0 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-lg"
				>
					<div class="flex h-full flex-col p-6">
						<div class="mb-4 flex items-center justify-between">
							<h2
								class="mb-3 text-xl leading-tight font-semibold text-gray-900 transition-colors group-hover:text-gray-700 dark:text-white"
							>
								{project.name}
							</h2>
							<span
								class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium {getStatusClass(
									project.status
								)}"
							>
								{getStatusText(project.status)}
							</span>
						</div>

						<!-- Description -->
						<div class="mb-6 flex-1">
							<p class="line-clamp-3 leading-relaxed text-gray-600 dark:text-gray-300">
								{project.description}
							</p>
						</div>

						<!-- Footer with dates and action -->
						<div class="mt-auto">
							<!-- Date Information -->
							<div class="mb-4 space-y-1">
								<div class="flex items-center justify-between text-sm">
									<span class="font-medium text-gray-500 dark:text-gray-400">Start Date</span>
									<time datetime={project.start_date} class="text-gray-700 dark:text-gray-300">
										{formatDate(project.start_date)}
									</time>
								</div>
								<div class="flex items-center justify-between text-sm">
									<span class="font-medium text-gray-500 dark:text-gray-400">End Date</span>
									<time datetime={project.end_date} class="text-gray-700 dark:text-gray-300">
										{formatDate(project.end_date)}
									</time>
								</div>
							</div>

							<!-- Action Button -->
							<Button
								size="sm"
								color="gray"
								class="w-full font-medium"
								href="/teams/{project.id}"
								data-sveltekit-preload-data="hover"
							>
								View Details
								<svg class="ml-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M9 5l7 7-7 7"
									></path>
								</svg>
							</Button>
						</div>
					</div>
				</Card>
			{/each}
		</div>
	{:else}
		<!-- Empty State dengan warna yang sama -->
		<div class="flex flex-col items-center justify-center py-20">
			<div
				class="mb-6 flex h-20 w-20 items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800"
			>
				<svg class="h-10 w-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="1.5"
						d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
					></path>
				</svg>
			</div>

			<h2 class="mb-2 text-2xl font-semibold text-gray-900 dark:text-white">No Projects Yet</h2>
			<p class="mb-6 max-w-md text-center text-gray-600 dark:text-gray-300">
				Start collaborating with your team by creating your first project. Organize tasks, set
				deadlines, and track progress together.
			</p>
		</div>
	{/if}
</div>

<style>
	.line-clamp-3 {
		display: -webkit-box;
		-webkit-line-clamp: 3;
		-webkit-box-orient: vertical;
		overflow: hidden;
	}
</style>
