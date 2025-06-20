<script lang="ts">
	import PageTask from '$lib/components/PageTask.svelte';
	import { Button, Card } from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { goto, invalidateAll, preloadData } from '$app/navigation';
	import { Section } from 'flowbite-svelte-blocks';
	import ActionModal from '$lib/components/ActionModal.svelte';
	import FormModal from '$lib/components/FormModal.svelte';
	import { enhance } from '$app/forms';
	import { CheckOutline, DeleteRowOutline } from 'flowbite-svelte-icons';

	let { data }: { data: PageData } = $props();
	let task = $state(data.task);
	let taskStatus = $state(task.status);
	let taskPriority = $state(task.priority);
	let taskTitle = $state(task.title);
	let taskDescription = $state(task.description);

	let taskValue = $state('');
	let subTasks = $state(data.subtask);

	let modal = $state(false);
	let deleteModal = $state(false);
	let updateModal = $state(false);

	let loadingUpdateStatus = $state(false);
	function statusEnhance() {
		loadingUpdateStatus = true;
		return async ({ result, update, formData }: any) => {
			loadingUpdateStatus = false;
			if (result.type === 'success') {
				taskStatus = formData.get('status') as string;
				await invalidateAll();
				await update();
			}
		};
	}

	let loadingUpdatePriority = $state(false);
	function priorityEnhance() {
		loadingUpdatePriority = true;
		return async ({ result, update, formData }: any) => {
			loadingUpdatePriority = false;
			if (result.type === 'success') {
				taskPriority = formData.get('priority') as string;
				await invalidateAll();
				await update();
			}
		};
	}

	function delay(ms: number) {
		return new Promise((resolve) => setTimeout(resolve, ms));
	}

	let loadingDelete = $state(false);
	function deleteEnhance() {
		loadingDelete = true;

		return async ({ result }: any) => {
			try {
				console.log(result);

				if (result.type === 'redirect') {
					await delay(400);
					await preloadData(result.location);
					goto(result.location);
				}
			} catch (error) {
				console.error('Error in deleteEnhance:', error);
			} finally {
				loadingDelete = false;
			}
		};
	}

	let loadingUpdate = $state(false);
	let successMessage = $state('');
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

	let selectedDate = $state(task?.due_date ? new Date(task.due_date) : undefined);

	let loadingSubtask = $state(false);
	function subtaskEnhance() {
		loadingSubtask = true;
		successMessage = '';
		return async ({ result, update, formData }: any) => {
			try {
				if (result.type === 'success') {
					successMessage = 'Success Update Task, redirecting...';
					await delay(350);
					location.reload();
					return;
				}
				await update();
			} catch (error) {
				console.error('Error in updateEnhance:', error);
			} finally {
				loadingSubtask = false;
			}
		};
	}
	console.log(subTasks);

	let loadingDeleteSubtask = $state(false);
	function subtaskDelete() {
		return async ({ result, update, formData }: any) => {
			loadingDeleteSubtask = true;
			try {
				if (result.type === 'success') {
					successMessage = 'Success Update Task, redirecting...';
					await delay(350);
					location.reload();
					return;
				}
				await update();
			} catch (error) {
				console.error('Error in updateEnhance:', error);
			} finally {
				loadingDeleteSubtask = false;
			}
		};
	}

	let loadingDoneSubtask = $state(false);
	function subtaskDone() {
		return async ({ result, update, formData }: any) => {
			loadingDoneSubtask = true;
			try {
				if (result.type === 'success') {
					successMessage = 'Success Update Task, redirecting...';
					await delay(350);
					location.reload();
					return;
				}
				await update();
			} catch (error) {
				console.error('Error in updateEnhance:', error);
			} finally {
				loadingDoneSubtask = false;
			}
		};
	}
</script>

<PageTask
	created={task.created_at}
	duedated={task.due_date}
	updated={task.updated_at}
	title={taskTitle}
	description={taskDescription}
	id={task.id}
	status={taskStatus}
	priority={taskPriority}
	updateStatusEnhance={statusEnhance}
	updatePriorityEnhance={priorityEnhance}
	updateStatusAction="?/updateStatus"
	updatePriorityAction="?/updatePriority"
	subtask={taskValue}
	subtaskAction="?/subtask"
	{subtaskEnhance}
	{loadingSubtask}
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
	{#snippet todoContent()}
		{#if subTasks && subTasks.length > 0}
			{#each subTasks as subtask (subtask.id)}
				<div class="border-t-2 border-dashed border-gray-300">
					<div class="my-2 flex gap-5">
						<Card
							class="w max-w-auto flex h-12 justify-center shadow-none transition-all delay-100 duration-300 hover:translate-y-2 hover:bg-gray-300 hover:shadow-sm"
						>
							<p
								class="ml-4 text-lg font-medium {subtask.is_done
									? 'text-gray-500 line-through'
									: 'text-gray-800'}"
							>
								{subtask.task}
							</p>
						</Card>
						<Card
							class="h-12 max-w-fit flex-row items-center justify-center gap-1 px-2 shadow-none"
						>
							<form action="?/deleteSubtask" method="POST" use:enhance={subtaskDelete}>
								<input type="hidden" id="subtask" name="subtask" value={subtask.id} />
								<button class="rounded-lg px-4 py-2" type="submit">
									<DeleteRowOutline
										class="mx-1 cursor-pointer text-gray-800 transition-all delay-150 duration-300 hover:translate-y-1 hover:text-red-800"
									/>
								</button>
							</form>

							<form action="?/done" method="POST" use:enhance={subtaskDone}>
								<input type="hidden" id="subtask" name="subtask" value={subtask.id} />
								<button class="rounded-lg px-4 py-2" type="submit">
									<CheckOutline
										class="mx-1 cursor-pointer text-gray-800 transition-all delay-150 duration-300 hover:translate-y-1 hover:text-green-800"
									/>
								</button>
							</form>
						</Card>
					</div>
				</div>
			{/each}
		{:else}
			<div class="rounded-lg border-2 border-dashed border-gray-300 bg-gray-50 p-8 text-center">
				<div class="text-gray-400">
					<svg class="mx-auto mb-4 h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
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
		{/if}
	{/snippet}
</PageTask>

<Section sectionClass="h-96">
	<ActionModal
		bind:open={modal}
		loading={deleteModal}
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
