<script lang="ts">
	import { Alert, Button, Card, Dropdown, DropdownItem, Modal } from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { enhance } from '$app/forms';
	import { goto, invalidateAll } from '$app/navigation';

	let { data }: { data: PageData } = $props();
	let task = $state(data.task);
	let isExpanded = $state(false);
	let modal = $state(false);

	// Simple check based on character length
	let showReadMore = $derived.by(() => (task?.description?.length || 0) > 100);

	function toggleExpanded() {
		isExpanded = !isExpanded;
	}

	function formatDate(dateString: string | undefined): string {
		if (!dateString) return 'N/A';
		return new Date(dateString).toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			hour: 'numeric',
			minute: '2-digit',
			hour12: true
		});
	}

	function getStatusStyle(status: string) {
		switch (status) {
			case 'todo':
				return 'bg-red-100 text-red-800 border-red-200';
			case 'in_progress':
				return 'bg-yellow-100 text-yellow-800 border-yellow-200';
			case 'completed':
				return 'bg-green-100 text-green-800 border-green-200';
			case 'canceled':
				return 'bg-red-400 text-red-800 border-red-600';
			default:
				return 'bg-gray-100 text-gray-800 border-gray-200';
		}
	}

	// Fungsi untuk style hover button
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

	function getStatusLabel(status: string | undefined) {
		return status ? status.replace('_', ' ').toUpperCase() : '';
	}

	let taskStatus = $state(task?.status ?? '');

	let loading = $state(false);

	// Hapus handleStatusChange lama, ganti jadi trigger submit form
	function handleStatusChange(status: string) {
		const form = document.getElementById('status-form') as HTMLFormElement;
		if (form) {
			(form.querySelector('input[name="status"]') as HTMLInputElement).value = status;
			form.requestSubmit();
		}
	}

	function customEnhance() {
		loading = true;

		return async ({ result, update, formData }) => {
			loading = false;
			if (result.type === 'success') {
				// Update state lokal agar UI langsung berubah tanpa reload manual
				taskStatus = formData.get('status') as string;
				await invalidateAll();
				await update();
			}
		};
	}

	let LoadingDelete = $state(false);
	function delay(ms: number) {
		return new Promise((resolve) => setTimeout(resolve, ms));
	}

	function deleteEnhance() {
		LoadingDelete = true;

		return async ({ result }) => {
			console.log(result);
			LoadingDelete = false;

			if (result.type === 'redirect') {
				await delay(2000); // delay 2 detik
				await invalidateAll();
				goto(result.location);
			}
		};
	}
</script>

<div class="m-4 flex items-center justify-center p-4">
	<Card
		class="h-auto w-full max-w-xl shadow-xl transition-all duration-300 hover:scale-105 hover:shadow-2xl md:h-120 md:max-w-2xl"
	>
		<div class="m-4 flex h-full flex-col items-start pb-4">
			<div class="mb-6 flex w-full items-center justify-between">
				<h1
					class="mr-4 line-clamp-2 flex-1 text-xl leading-tight font-bold text-black transition-colors duration-200 hover:text-gray-400"
				>
					{task?.title}
				</h1>

				<form id="status-form" action="?/updateStatus" method="post" use:enhance={customEnhance}>
					<input type="hidden" name="taskId" value={task?.id} />
					<input type="hidden" name="status" value={taskStatus} />
					<div class="flex items-center gap-3">
						<Button
							class="rounded-full border px-3 py-1 text-xs font-semibold transition-all duration-200 {getStatusButtonStyle(
								taskStatus || ''
							)}"
							size="xs"
							type="button"
						>
							{getStatusLabel(taskStatus)}
						</Button>

						<Dropdown simple class="border-none shadow-none">
							<DropdownItem class="w-full bg-red-100" onclick={() => handleStatusChange('todo')}
								>To Do</DropdownItem
							>
							<DropdownItem
								class="w-full bg-yellow-100"
								onclick={() => handleStatusChange('in_progress')}>In Progress</DropdownItem
							>
							<DropdownItem
								class="w-full bg-green-100"
								onclick={() => handleStatusChange('completed')}>Completed</DropdownItem
							>
							<DropdownItem class="w-full bg-red-400" onclick={() => handleStatusChange('canceled')}
								>Canceled</DropdownItem
							>
						</Dropdown>
					</div>
				</form>
			</div>

			<div class="mb-4 w-full flex-1">
				<p
					class="text-sm leading-relaxed text-black transition-all duration-300 {isExpanded
						? ''
						: 'line-clamp-2'}"
				>
					{task?.description}
				</p>

				{#if showReadMore}
					<button
						class="mt-2 text-sm font-medium text-gray-600 underline transition-colors duration-200 hover:text-gray-800 focus:outline-none"
						onclick={toggleExpanded}
					>
						{isExpanded ? 'Show less' : 'Show more'}
					</button>
				{/if}
			</div>

			<div class="flex-grow"></div>

			<div class="mt-auto w-full">
				<!-- Timeline Section -->
				<div class="mb-6 space-y-3">
					<h3 class="mb-3 text-sm font-semibold text-gray-600">Timeline</h3>

					<div class="grid grid-cols-1 gap-3 sm:grid-cols-3">
						<!-- Created -->
						<div class="rounded-lg border border-slate-700/50 bg-gray-800 p-3">
							<div class="mb-1 flex items-center gap-2">
								<span class="text-sm">🎯</span>
								<span class="text-xs font-medium tracking-wide text-white uppercase">Created</span>
							</div>
							<time class="text-sm font-medium text-white" datetime={task?.created_at}>
								{formatDate(task?.created_at)}
							</time>
						</div>

						<!-- Updated -->
						<div class="rounded-lg border border-slate-700/50 bg-gray-800 p-3">
							<div class="mb-1 flex items-center gap-2">
								<span class="text-sm">🔄</span>
								<span class="text-xs font-medium tracking-wide text-white uppercase">Updated</span>
							</div>
							<time class="text-sm font-medium text-white" datetime={task?.updated_at}>
								{formatDate(task?.updated_at)}
							</time>
						</div>

						<!-- Due Date -->
						<div class="rounded-lg border border-slate-700/50 bg-gray-800 p-3">
							<div class="mb-1 flex items-center gap-2">
								<span class="text-sm">📅</span>
								<span class="text-xs font-medium tracking-wide text-white uppercase">Due Date</span>
							</div>
							<time class="text-sm font-medium text-white" datetime={task?.due_dated_at}>
								{formatDate(task?.due_dated_at)}
							</time>
						</div>
					</div>
				</div>

				<!-- Action Buttons -->
				<div class="border-t border-slate-700/50 pt-4">
					<div class="flex flex-wrap justify-end gap-3">
						<Button
							color="light"
							class="border-slate-600 bg-gray-800 text-white transition-all duration-200 hover:bg-gray-900 hover:text-gray-300"
						>
							<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
								></path>
							</svg>
							Edit
						</Button>

						<Button
							color="red"
							class="border-red-500/30 bg-red-800 text-white transition-all duration-200 hover:bg-red-900 hover:text-red-300"
							onclick={() => {
								modal = true;
							}}
						>
							<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
								></path>
							</svg>
							Delete
						</Button>
					</div>
				</div>
			</div>
		</div>
	</Card>
</div>

<Modal bind:open={modal} size="sm" class="w-full">
	{#snippet header()}
		<div class="flex items-center gap-3">
			<div class="flex h-12 w-12 items-center justify-center rounded-full bg-red-100">
				<svg class="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16c-.77.833.192 2.5 1.732 2.5zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
					></path>
				</svg>
			</div>
			<div>
				<h3 class="text-lg font-semibold text-gray-900 dark:text-white">Delete Task</h3>
				<p class="text-sm text-gray-500 dark:text-gray-400">This action cannot be undone</p>
			</div>
		</div>
	{/snippet}

	<div class="space-y-4">
		<p class="text-sm text-gray-600 dark:text-gray-300">
			Are you sure you want to delete "<span class="font-semibold">{task?.title}</span>"? This will
			permanently remove the task and all associated data.
		</p>

		<Alert class="border-red-200 bg-red-50 text-red-800">
			<svg class="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
				<path
					fill-rule="evenodd"
					d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
					clip-rule="evenodd"
				></path>
			</svg>
			<span class="font-medium">Warning!</span> This action is permanent and cannot be reversed.
		</Alert>
	</div>

	{#snippet footer()}
		<div class="flex justify-end gap-3">
			<Button color="alternative" onclick={() => (modal = false)}>Cancel</Button>
			<form action="?/delete" method="post" use:enhance={deleteEnhance}>
				<input type="hidden" name="id" value={task?.id} />
				<Button color="red" type="submit" disabled={LoadingDelete}>
					<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
						></path>
					</svg>
					{LoadingDelete ? 'Redirecting....' : 'Delete Task'}
				</Button>
			</form>
		</div>
	{/snippet}
</Modal>
