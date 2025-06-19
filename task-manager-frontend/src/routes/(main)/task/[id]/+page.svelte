<script lang="ts">
	import { Button } from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { goto, invalidateAll, preloadData } from '$app/navigation';
	import { Section } from 'flowbite-svelte-blocks';
	import FormModal from '$lib/components/FormModal.svelte';
	import ActionModal from '$lib/components/ActionModal.svelte';
	import PageTask from '$lib/components/PageTask.svelte';

	let { data }: { data: PageData } = $props();
	let task = $state(data.task);
	let isExpanded = $state(false);
	let modal = $state(false);
	let updateModal = $state(false);

	let taskStatus = $state(task?.status ?? '');
	let taskTitle = $state(task.title);
	let taskDescription = $state(task.description);

	let loading = $state(false);
	let loadingDelete = $state(false);
	let loadingUpdate = $state(false);
	let successMessage = $state('');
	let selectedDate = $state(task?.due_dated_at ? new Date(task.due_dated_at) : undefined);

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

		return async ({ result, update, formData }: any) => {
			try {
				if (result.type === 'success') {
					successMessage = 'Success Update Task, redirecting...';

					await delay(350);
					taskTitle = formData.get('title') as string;
					taskDescription = formData.get('description') as string;
					updateModal = false;
					return;
				}
				await update();
			} catch (error) {
				console.error('Error in updateEnhance:', error);
			} finally {
				loadingUpdate = false;
			}
		};
	}
</script>

<PageTask
	created={task.created_at}
	duedated={task.due_dated_at}
	updated={task.updated_at}
	title={taskTitle}
	description={taskDescription}
	status={taskStatus}
	enhance={customEnhance}
	id={task.id}
>
	{#snippet hasContent()}
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
	{/snippet}
</PageTask>

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
