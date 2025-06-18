<script lang="ts">
	import { Button, Card, Dropdown, DropdownItem, Modal } from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { enhance } from '$app/forms';
	import { goto, invalidateAll, preloadData } from '$app/navigation';
	import { Section } from 'flowbite-svelte-blocks';
	import FormModal from '$lib/components/FormModal.svelte';
	import ActionModal from '$lib/components/ActionModal.svelte';

	let { data }: { data: PageData } = $props();
	let task = $state(data.task);
	let isExpanded = $state(false);
	let modal = $state(false);
	let updateModal = $state(false);

	let showReadMore = $derived.by(() => (task?.description?.length || 0) > 100);

	let taskStatus = $state(task?.status ?? '');

	let loading = $state(false);
	let loadingDelete = $state(false);
	let loadingUpdate = $state(false);
	let successMessage = $state('');
	let selectedDate = $state(task?.due_dated_at ? new Date(task.due_dated_at) : undefined);

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

	function handleStatusChange(status: string) {
		const form = document.getElementById('status-form') as HTMLFormElement;
		if (form) {
			(form.querySelector('input[name="status"]') as HTMLInputElement).value = status;
			form.requestSubmit();
		}
	}

	function customEnhance() {
		loading = true;

		return async ({ result, update, formData }: any) => {
			loading = false;
			if (result.type === 'success') {
				taskStatus = formData.get('status') as string;
				await invalidateAll();
				await update();
			}
		};
	}

	function delay(ms: number) {
		return new Promise((resolve) => setTimeout(resolve, ms));
	}

	function deleteEnhance() {
		loadingDelete = true;

		return async ({ result }: any) => {
			try {
				console.log(result);

				if (result.type === 'success') {
					await delay(350);
					await preloadData('/task');
					goto('/task');
				}
			} catch (error) {
				console.error('Error in deleteEnhance:', error);
			} finally {
				loadingDelete = false;
			}
		};
	}

	function updateEnhance() {
		loadingUpdate = true;
		successMessage = '';

		return async ({ result, update }: any) => {
			try {
				if (result.type === 'success') {
					successMessage = 'Success Update Task, redirecting...';

					await delay(350);
					updateModal = false;
					location.reload();
					return;
				}
				await update();
			} catch (error) {
				console.error('Error in deleteEnhance:', error);
			} finally {
				loadingUpdate = false;
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
							onclick={() => {
								updateModal = true;
							}}
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

<Section sectionClass="h-96">
	<ActionModal
		bind:open={modal}
		loading={loadingDelete}
		id={task?.id?.toString()}
		enhance={deleteEnhance}
		question="Are you sure you want to delete this Task {task?.title}?"
		isDelete
		action="?/delete"
	/>
</Section>

<FormModal
	bind:open={updateModal}
	action="?/updateTask"
	size="md"
	formTitle="Update Task"
	formDescription="Update your current task to new task"
	first_field="Title"
	first_field_value={task.title}
	second_field="Description"
	second_field_value={task.description}
	loading={loadingUpdate}
	enhance={updateEnhance}
	datepicker
	update
	third_field="Due Date Task"
	formDate={selectedDate}
	hidden_field="taskId"
	hidden_field_value={task.id}
/>
