<script lang="ts">
	import {
		Alert,
		Button,
		Card,
		Datepicker,
		Dropdown,
		DropdownItem,
		Input,
		Label,
		Modal,
		Textarea
	} from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { enhance } from '$app/forms';
	import { goto, invalidateAll } from '$app/navigation';
	import { slide } from 'svelte/transition';
	import { formatToISO } from '$lib/utils/formatDate';
	import { Section } from 'flowbite-svelte-blocks';

	let { data }: { data: PageData } = $props();
	let task = $state(data.task);
	let isExpanded = $state(false);
	let modal = $state(false);
	let updateModal = $state(false);

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

		return async ({ result, update, formData }: any) => {
			loading = false;
			if (result.type === 'success') {
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

		return async ({ result }: any) => {
			console.log(result);
			LoadingDelete = false;

			if (result.type === 'redirect') {
				await delay(1500);
				await invalidateAll();
				goto(result.location);
			}
		};
	}

	let loadingUpdate = $state(false);
	let successMessage = $state('');
	function updateEnhance() {
		loadingUpdate = true;
		successMessage = '';

		return async ({ result, update }: any) => {
			loadingUpdate = false;
			if (result.type === 'success') {
				successMessage = 'Success Update Task, redirecting...';
				await invalidateAll();

				setTimeout(() => {
					updateModal = false;
					successMessage = '';
					location.reload();
				}, 1000);
				return;
			}

			await update();
		};
	}

	// Tambahkan state untuk date picker
	let selectedDate = $state(task?.due_dated_at ? new Date(task.due_dated_at) : undefined);

	// Function untuk handle perubahan date
	function handleDateChange(event: CustomEvent) {
		selectedDate = event.detail;
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
	<Modal title="" bind:open={modal} autoclose size="sm" class="w-full">
		<svg
			class="mx-auto mb-3.5 h-11 w-11 text-gray-400 dark:text-gray-500"
			aria-hidden="true"
			fill="currentColor"
			viewBox="0 0 20 20"
			xmlns="http://www.w3.org/2000/svg"
			><path
				fill-rule="evenodd"
				d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z"
				clip-rule="evenodd"
			/></svg
		>
		<p class="mb-4 text-center text-gray-500 dark:text-gray-300">
			Are you sure you want to delete this Task {task?.title}?
		</p>
		<div class="flex items-center justify-center space-x-4">
			<Button color="light" onclick={() => (modal = false)}>No, Cancel</Button>
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
					{LoadingDelete ? 'Redirecting....' : `Yes, I'm sure`}
				</Button>
			</form>
		</div>
	</Modal>
</Section>

<Modal bind:open={updateModal} autoclose={false} size="md" transition={slide}>
	<div class="flex justify-center">
		<Card class="max-w-full rounded-none border-none p-4 shadow-none sm:p-6 md:p-8">
			<form
				class="flex flex-col space-y-6"
				method="POST"
				use:enhance={updateEnhance}
				action="?/updateTask"
			>
				<Input type="hidden" name="taskId" value={task?.id} />
				<h3 class="text-xl font-bold text-gray-900 dark:text-white">Update Task</h3>

				<Label for="title">
					<span>Title</span>
					<Input
						value={task?.title}
						type="text"
						name="title"
						placeholder="Task title"
						id="title"
						color="gray"
						required
						disabled={loadingUpdate}
					/>
				</Label>

				<Label for="description">
					<span>Description</span>
					<Textarea
						value={task?.description}
						id="description"
						placeholder="Task description"
						color="gray"
						rows={4}
						name="description"
						disabled={loadingUpdate}
					/>
				</Label>

				<Label for="date">
					<span>Due Date</span>
					<Datepicker
						bind:value={selectedDate}
						color="gray"
						id="datepicker"
						disabled={loadingUpdate}
					/>
					<Input
						type="hidden"
						name="date"
						value={selectedDate ? formatToISO(selectedDate.toISOString()) : ''}
					/>
				</Label>

				<div class="flex gap-3">
					<Button
						type="submit"
						class="flex-1 bg-gray-700 text-white hover:bg-gray-900"
						disabled={loadingUpdate}
					>
						{loadingUpdate ? 'Updating...' : 'Update Task'}
					</Button>
					<Button
						color="alternative"
						onclick={() => (updateModal = false)}
						disabled={loadingUpdate}
						type="button"
					>
						Cancel
					</Button>
				</div>
			</form>
		</Card>
	</div>
</Modal>
