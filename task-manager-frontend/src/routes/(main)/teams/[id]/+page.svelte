<script lang="ts">
	import { Breadcrumb, BreadcrumbItem, Button, Card, Modal } from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { formatDate } from '$lib/utils/formatDate';
	import { AngleLeftOutline, AngleRightOutline } from 'flowbite-svelte-icons';
	import {
		getLevelText,
		getPriorityStyle,
		getPriorityText,
		getStatusClass,
		getStatusLabel,
		getStatusStyle,
		getStatusText
	} from '$lib/utils/label';
	import { goto, invalidateAll, preloadData } from '$app/navigation';
	import { Section } from 'flowbite-svelte-blocks';
	import FormModal from '$lib/components/FormModal.svelte';
	import ActionModal from '$lib/components/ActionModal.svelte';
	import TaskCard from '$lib/components/TaskCard.svelte';
	import TagMember from '$lib/components/TagMember.svelte';

	let { data }: { data: PageData } = $props();
	let project = $state(data.project);
	let tasks = $state(data.tasks);
	let members = $state(data.members);
	let modal = $state(false);
	let modalUpdate = $state(false);
	let loadingUpdate = $state(false);
	let loadingDelete = $state(false);
	let modalArchive = $state(false);
	let modalDelete = $state(false);
	let isArchived = $derived.by(() => {
		if (project?.status === 'archive') {
			return true;
		}
		return false;
	});
	let projectStatus = $state(project?.status);
	console.log(members);

	function formatRelativeDate(dateString: string): string {
		const date = new Date(dateString);
		const now = new Date();
		const diffInDays = Math.ceil((date.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));

		if (diffInDays === 0) return 'Today';
		if (diffInDays === 1) return 'Tomorrow';
		if (diffInDays === -1) return 'Yesterday';
		if (diffInDays > 0) return `In ${diffInDays} days`;
		if (diffInDays < 0) return `${Math.abs(diffInDays)} days ago`;

		return formatDate(dateString);
	}

	let title = $state('');
	let description = $state('');
	let dueDate = $state('');
	let date = $state<Date | undefined>(undefined);
	let loading = $state(false);
	let errorMessages = $state<string[]>([]);
	let successMessage = $state('');
	let selected = $state();

	function closeModal() {
		modal = false;
		errorMessages = [];
		loading = false;
		resetForm();
	}

	function delay(ms: number) {
		return new Promise((resolve) => setTimeout(resolve, ms));
	}

	function customEnhance() {
		loading = true;
		errorMessages = [];
		successMessage = '';

		return async ({ result, update }: any) => {
			console.log('Form result:', result);

			try {
				if (result.type === 'success') {
					successMessage = 'Success Created New Task, redirecting....';
					closeModal();
					successMessage = '';
					await preloadData(`/teams/${project?.id}`);
					location.reload();

					return;
				} else if (result.type === 'failure') {
					const data = result.data;
					const { validations, error } = data || {};

					if (validations && Array.isArray(validations)) {
						errorMessages = validations;
					} else if (error) {
						errorMessages = [error];
					} else {
						errorMessages = ['An error occurred. Please try again.'];
					}
				}

				await update();
			} catch (error) {
				console.error('Error in deleteEnhance:', error);
			} finally {
				loading = false;
			}
		};
	}

	function resetForm() {
		title = '';
		description = '';
		dueDate = '';
		errorMessages = [];
		date = undefined;
		selected = '';
	}

	let loadingArchive = $state(false);
	function archiveEnhanced() {
		loadingArchive = true;

		return async ({ result, update, formData }: any) => {
			try {
				if (result.type === 'success') {
					projectStatus = formData.get('status') as string;
					isArchived = projectStatus === 'archive';
					await delay(300);
					modalArchive = false;
				}
			} catch (error) {
				console.error('Error in deleteEnhance:', error);
			} finally {
				loadingArchive = false;
			}
		};
	}

	let selectedEndDate = $state(project?.end_date ? new Date(project.end_date) : undefined);
	let selectedStartDate = $state(project?.end_date ? new Date(project.end_date) : undefined);
	let projectName = $state(project?.name);
	let projectDescription = $state(project?.description);
	let projectEnd = $state(project?.end_date);
	let projectStart = $state(project?.start_date);

	function updateEnhanced() {
		loadingUpdate = true;

		return async ({ result, formData }: any) => {
			console.log(result);
			try {
				if (result.type === 'success') {
					projectName = formData.get('name') as string;
					projectDescription = formData.get('description') as string;
					projectStart = formData.get('start_date') as string;
					projectEnd = formData.get('end_date') as string;
					await delay(500);
					modalUpdate = false;
				}
			} catch (error) {
				console.error('Error in deleteEnhance:', error);
			} finally {
				loadingUpdate = false;
			}
		};
	}

	function deleteEnhance() {
		loadingDelete = true;

		return async ({ result }: any) => {
			try {
				if (result.type === 'success') {
					await delay(300);
					await preloadData('/teams');
					goto('/teams');
				}
			} catch (error) {
				console.error('Error in deleteEnhance:', error);
			} finally {
				loadingDelete = false;
			}
		};
	}

	function getStyle(isArchived: boolean) {
		switch (isArchived) {
			case false:
				return 'bg-blue-300 hover:bg-blue-600';
			case true:
				return 'bg-green-300 hover:bg-green-600';
		}
	}
</script>

<div class="container m-3 p-3 md:m-4 md:border-b-1 md:border-b-gray-400 md:p-4">
	<div class="mb-4 grid gap-6 md:grid-cols-2">
		<div class="mb-10 text-center md:mb-8 md:text-left">
			<div class="space-y-6">
				<div class="mb-6 space-y-3">
					<div
						class="flex flex-col items-center gap-3 md:flex-row md:items-center md:justify-start"
					>
						<h1 class="text-3xl font-bold text-gray-900 md:text-4xl dark:text-white">
							{projectName}
						</h1>
						<span
							class="inline-flex items-center rounded-full border px-3 py-1 text-sm font-medium shadow-sm {getStatusClass(
								projectStatus || ''
							)}"
						>
							{getStatusText(projectStatus || '')}
						</span>
					</div>
					<p class="max-w-2xl text-lg leading-relaxed text-gray-600 dark:text-gray-300">
						{projectDescription}
					</p>
				</div>

				<div class="hidden flex-wrap md:block">
					{#if members && members.length > 0}
						<div class="flex flex-wrap items-center gap-2">
							{#each members as member (member.id)}
								<TagMember username={member.username} level={member.level} status={member.status} />
							{/each}
						</div>
					{/if}
				</div>
			</div>
		</div>
		<div class="hidden md:block">
			<div class="mb-8"></div>
			<Card class="mb-4 max-w-full border-none shadow-none">
				<div class="flex items-center justify-between">
					<span class="font-medium text-gray-500 dark:text-gray-400">Created by</span>
					<span class="font-medium text-gray-900 dark:text-white">{project?.createdBy}</span>
				</div>

				<div class="space-y-2 border-t pt-3 dark:border-gray-700">
					<div class="flex items-center justify-between text-sm">
						<span class="text-gray-500 dark:text-gray-400">Start Date</span>
						<div class="text-right">
							<div class="font-medium text-gray-900 dark:text-white">
								{formatDate(projectStart || '')}
							</div>
							<div class="text-xs text-gray-500">
								{formatRelativeDate(projectStart || '')}
							</div>
						</div>
					</div>

					<div class="flex items-center justify-between text-sm">
						<span class="text-gray-500 dark:text-gray-400">End Date</span>
						<div class="text-right">
							<div class="font-medium text-gray-900 dark:text-white">
								{formatDate(projectEnd || '')}
							</div>
							<div class="text-xs text-gray-500">
								{formatRelativeDate(projectEnd || '')}
							</div>
						</div>
					</div>
				</div>

				<!-- Created At -->
				<div class="border-t pt-3 dark:border-gray-700">
					<div class="flex items-center justify-between text-sm">
						<span class="text-gray-500 dark:text-gray-400">Created</span>
						<div class="text-right">
							<div class="text-xs text-gray-500">
								{formatDate(project?.created_at || '')}
							</div>
						</div>
					</div>
				</div>
			</Card>
			<div class="flex justify-between gap-3">
				<div>
					<Card class="p-4">
						<h3 class="mb-3 font-semibold text-gray-900 dark:text-white">Quick Stats</h3>
						<div class="space-y-2">
							<div class="flex justify-between text-sm">
								<span class="text-gray-500">Total Tasks</span>
								<span class="font-medium">{tasks?.length}</span>
							</div>
							<div class="flex justify-between text-sm">
								<span class="text-gray-500">Todo</span>
								<span class="font-medium text-yellow-600"
									>{tasks?.filter((task) => task.status === 'todo').length || 0}</span
								>
							</div>
							<div class="flex justify-between text-sm">
								<span class="text-gray-500">Completed</span>
								<span class="font-medium text-green-600"
									>{tasks?.filter((task) => task.status === 'completed').length || 0}</span
								>
							</div>
							<div class="flex justify-between text-sm">
								<span class="text-gray-500">In Progress</span>
								<span class="font-medium text-gray-600"
									>{tasks?.filter((task) => task.status === 'in_progress').length || 0}</span
								>
							</div>
							<div class="flex justify-between text-sm">
								<span class="text-gray-500">Cancel</span>
								<span class="font-medium text-red-600"
									>{tasks?.filter((task) => task.status === 'canceled').length || 0}</span
								>
							</div>
						</div>
					</Card>
				</div>

				<div>
					<Card class="p-4">
						<h3 class="mb-3 font-semibold text-gray-900 dark:text-white">Actions</h3>
						<div class="space-y-2">
							<Button
								class="w-full rounded-lg border hover:text-gray-800"
								color="alternative"
								disabled={isArchived}
								onclick={() => {
									modalUpdate = true;
								}}
							>
								Edit Project
							</Button>
							<Button
								class="w-full rounded-lg border hover:text-gray-800"
								color="alternative"
								onclick={() => {
									modal = true;
								}}
								disabled={isArchived}
							>
								Add Task
							</Button>
							<div class="flex gap-1">
								<Button
									class="w-full rounded-lg border {getStyle(isArchived)}"
									onclick={() => {
										modalArchive = true;
									}}
								>
									{isArchived ? 'Active' : 'Archive'}
								</Button>
								<Button
									class="w-full rounded-lg border"
									color="red"
									onclick={() => {
										modalDelete = true;
									}}
								>
									Delete
								</Button>
							</div>
						</div>
					</Card>
				</div>
			</div>
		</div>
	</div>
</div>

<div class="m-4 p-4">
	{#if tasks && tasks.length > 0}
		<div
			class="flex flex-col items-center gap-6 md:grid md:grid-cols-2 md:items-stretch lg:grid-cols-3 xl:grid-cols-4"
		>
			{#each tasks as task (task.id)}
				<TaskCard title={task.title} date={task.due_date}>
					{#snippet tRcontent(hovered: boolean)}
						{#if hovered}
							<AngleRightOutline
								class="cursor-pointer"
								size="md"
								onclick={() => {
									goto(`/teams/${project?.id}/task/${task.id}`);
								}}
							/>
						{/if}
					{/snippet}
					{#snippet bLcontent()}
						<span
							class="rounded-full border px-3 py-1 text-xs font-semibold {getPriorityStyle(
								task.priority
							)}"
						>
							{getPriorityText(task.priority)}
						</span>
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

<FormModal
	bind:open={modal}
	action="?/create"
	enhance={customEnhance}
	size="md"
	formTitle="Create Task Project"
	formDescription="Set up a new task with due date and description"
	{errorMessages}
	{successMessage}
	first_field="Title"
	first_field_value={title}
	second_field="description"
	second_field_value={description}
	{loading}
	datepicker
	third_field="Due Date"
	formDate={date}
	isSelected
/>

<FormModal
	bind:open={modalUpdate}
	action="?/update"
	enhance={updateEnhanced}
	loading={loadingUpdate}
	size="md"
	formTitle="Update Current Project"
	formDescription="Update your current project to new project"
	first_field="Name"
	first_field_value={project?.name}
	second_field="Description"
	second_field_value={project?.description}
	datepicker
	isDatePickerRange
	update
	third_field="End Date Project"
	formDate={selectedEndDate}
	toDate={selectedStartDate}
	hidden_field="projectId"
	hidden_field_value={project?.id}
/>

<Section sectionClass="h-96">
	<ActionModal
		bind:open={modalArchive}
		loading={loadingArchive}
		id={project?.id.toString()}
		enhance={archiveEnhanced}
		question="Are you sure you want to {isArchived ? 'Active' : 'Archive'} this Project?"
		action="?/archive"
		status={isArchived ? 'active' : 'archive'}
	/>
</Section>

<Section sectionClass="h-96">
	<ActionModal
		bind:open={modalDelete}
		loading={loadingDelete}
		id={project?.id.toString()}
		enhance={deleteEnhance}
		question="Are you sure you want to delete this Project {projectName} ?"
		action="?/delete"
		isDelete
	/>
</Section>

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
