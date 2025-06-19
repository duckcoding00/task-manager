<script lang="ts">
	import { enhance } from '$app/forms';
	import { formatDate, formatToDateString } from '$lib/utils/formatDate';
	import { getStatusLabel } from '$lib/utils/label';
	import type { SubmitFunction } from '@sveltejs/kit';
	import { Button, Dropdown, DropdownItem, Timeline, TimelineItem } from 'flowbite-svelte';
	import type { Snippet } from 'svelte';

	interface props {
		created?: string;
		duedated?: string;
		updated?: string;
		title?: string;
		description?: string;
		status?: string;
		id?: number;

		enhance?: SubmitFunction;
		hasContent?: Snippet;
	}

	let {
		created,
		duedated,
		updated,
		title,
		description,
		status,
		id,
		enhance: enhanceFunction,
		hasContent
	}: props = $props();

	// Status color mapping
	const getStatusColor = (status: string) => {
		switch (status?.toLowerCase()) {
			case 'completed':
			case 'done':
				return 'bg-green-100 text-green-800 border-green-200';
			case 'in progress':
			case 'ongoing':
				return 'bg-blue-100 text-blue-800 border-blue-200';
			case 'todo':
			case 'waiting':
				return 'bg-yellow-100 text-yellow-800 border-yellow-200';
			case 'canceled':
			case 'blocked':
				return 'bg-red-100 text-red-800 border-red-200';
			default:
				return 'bg-gray-100 text-gray-800 border-gray-200';
		}
	};

	function getStatusButtonStyle(status: string) {
		switch (status) {
			case 'todo':
				return 'bg-red-100 text-red-800 border-red-200 hover:bg-red-200 hover:border-red-300';
			case 'in_progress':
				return 'bg-yellow-100 text-yellow-800 border-yellow-200 hover:bg-yellow-200 hover:border-yellow-300';
			case 'completed':
				return 'bg-green-100 text-green-800 border-green-200 hover:bg-green-200 hover:border-green-300';
			case 'canceled':
				return 'bg-red-400 text-red-800 border-red-600 hover:bg-red-500 hover:border-red-700';
			default:
				return 'bg-gray-100 text-gray-800 border-gray-200 hover:bg-gray-200 hover:border-gray-300';
		}
	}

	function handleStatusChange(status: string) {
		const form = document.getElementById('status-form') as HTMLFormElement;
		if (form) {
			(form.querySelector('input[name="status"]') as HTMLInputElement).value = status;
			form.requestSubmit();
		}
	}
</script>

<div class="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm">
	<div class="grid min-h-[400px] grid-cols-1 lg:grid-cols-3">
		<div class="border-gray-200 bg-gray-50 lg:border-r">
			<div class="p-6">
				<div class="space-y-4">
					<div class="flex items-start justify-between gap-4">
						<h1 class="flex-1 text-2xl leading-tight font-bold text-gray-900">
							{title || 'Untitled Project'}
						</h1>
						{#if status}
							<form
								id="status-form"
								action="?/updateStatus"
								method="post"
								use:enhance={enhanceFunction}
							>
								<input type="hidden" name="taskId" value={id} />
								<input type="hidden" name="status" value={status} />
								<div class="flex items-center gap-3">
									<Button
										class="rounded-full border px-3 py-1 text-xs font-semibold transition-all duration-200 {getStatusColor(
											status || ''
										)}"
										size="xs"
										type="button"
									>
										{getStatusLabel(status)}
									</Button>

									<Dropdown simple class="border-none shadow-none">
										<DropdownItem
											class="w-full {getStatusColor('todo')}"
											onclick={() => handleStatusChange('todo')}>To Do</DropdownItem
										>
										<DropdownItem
											class="w-full {getStatusColor('in_progress')}"
											onclick={() => handleStatusChange('in_progress')}>In Progress</DropdownItem
										>
										<DropdownItem
											class="w-full {getStatusColor('completed')}"
											onclick={() => handleStatusChange('completed')}>Completed</DropdownItem
										>
										<DropdownItem
											class="w-full {getStatusColor('canceled')}"
											onclick={() => handleStatusChange('canceled')}>Canceled</DropdownItem
										>
									</Dropdown>
								</div>
							</form>
						{/if}
					</div>

					{#if description}
						<div class="rounded-lg border border-gray-200 bg-white p-4">
							<p class="leading-relaxed text-gray-700">{description}</p>
						</div>
					{/if}
				</div>

				<div class="mt-8 hidden lg:block">
					<h3 class="mb-4 text-lg font-semibold text-gray-900">Timeline</h3>
					<div class="space-y-4">
						<Timeline>
							{#if created}
								<TimelineItem title="" date={formatDate(created)}>
									<div class="flex items-center gap-2">
										<div class="h-2 w-2 rounded-full bg-green-500"></div>
										<p class="text-lg font-semibold text-gray-800">Created</p>
									</div>
								</TimelineItem>
							{/if}

							{#if duedated}
								<TimelineItem title="" date={formatDate(duedated)}>
									<div class="flex items-center gap-2">
										<div class="h-2 w-2 rounded-full bg-red-500"></div>
										<p class="text-lg font-semibold text-gray-800">Due Date</p>
									</div>
								</TimelineItem>
							{/if}

							{#if updated}
								<TimelineItem title="" date={formatDate(updated)}>
									<div class="flex items-center gap-2">
										<div class="h-2 w-2 rounded-full bg-blue-500"></div>
										<p class="text-lg font-semibold text-gray-800">Last Updated</p>
									</div>
								</TimelineItem>
							{/if}
						</Timeline>
					</div>
				</div>

				<div class="mt-6 block lg:hidden">
					<h3 class="mb-4 text-lg font-semibold text-gray-900">Timeline</h3>
					<div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
						{#if created}
							<div class="rounded-lg border-l-4 border-green-500 bg-white p-4 shadow-sm">
								<p class="mb-1 text-sm text-gray-500">Created</p>
								<p class="font-semibold text-gray-900">{formatDate(created)}</p>
							</div>
						{/if}

						{#if duedated}
							<div class="rounded-lg border-l-4 border-red-500 bg-white p-4 shadow-sm">
								<p class="mb-1 text-sm text-gray-500">Due Date</p>
								<p class="font-semibold text-gray-900">{formatDate(duedated)}</p>
							</div>
						{/if}

						{#if updated}
							<div class="rounded-lg border-l-4 border-blue-500 bg-white p-4 shadow-sm">
								<p class="mb-1 text-sm text-gray-500">Updated</p>
								<p class="font-semibold text-gray-900">{formatDate(updated)}</p>
							</div>
						{/if}
					</div>
				</div>

				<div class="mt-8 flex justify-end gap-1">
					{#if hasContent}
						{@render hasContent()}
					{/if}
				</div>
			</div>
		</div>

		<div class="lg:col-span-2">
			<div class="p-6">
				<div class="mb-6 flex items-center justify-between">
					<h2 class="text-2xl font-bold text-gray-900">Sub Tasks</h2>
					<button
						class="rounded-lg bg-blue-600 px-4 py-2 font-medium text-white transition-colors duration-200 hover:bg-blue-700"
					>
						+ Add Task
					</button>
				</div>

				<div class="rounded-lg border-2 border-dashed border-gray-300 bg-gray-50 p-8 text-center">
					<div class="text-gray-400">
						<svg
							class="mx-auto mb-4 h-12 w-12"
							fill="none"
							viewBox="0 0 24 24"
							stroke="currentColor"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"
							/>
						</svg>
						<p class="text-lg font-medium">No tasks yet</p>
						<p class="text-sm">Add your first task to get started</p>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
