<script lang="ts">
	import { Breadcrumb, BreadcrumbItem, Button, Card, Modal } from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { formatDate } from '$lib/utils/formatDate';
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

	let { data }: { data: PageData } = $props();
	let project = $state(data.project);
	let tasks = $state(data.tasks);
	let members = $state(data.members);
	let modal = $state(false);
	let modalArchive = $state(false);
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
					modalArchive = false;
				}
			} catch (error) {
				console.error('Error in deleteEnhance:', error);
			} finally {
				loadingArchive = false;
			}
		};
	}
</script>

<div class="container m-3 border-b-1 border-b-gray-400 p-3 md:m-4 md:p-4">
	<div class="mb-4 grid gap-6 md:grid-cols-2">
		<div class="mb-10 text-center md:mb-8 md:text-left">
			<Breadcrumb aria-label="Solid background breadcrumb example" class="mb-6 md:mb-4">
				<BreadcrumbItem href="/teams" home>Teams</BreadcrumbItem>
				<BreadcrumbItem href="#">{project?.name}</BreadcrumbItem>
			</Breadcrumb>
			<div class="space-y-4">
				<div class="space-y-1">
					<div class="mb-2 flex items-center justify-center gap-3 md:justify-start">
						<h1 class="text-2xl font-bold text-gray-900 md:text-3xl dark:text-white">
							{project?.name}
						</h1>
						<!-- Status Badge -->
						<span
							class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium {getStatusClass(
								projectStatus || ''
							)}"
						>
							{getStatusText(projectStatus || '')}
						</span>
					</div>
					<p class="text-lg text-gray-600 dark:text-gray-300">
						{project?.description}
					</p>
				</div>
				<div class="hidden md:block">
					{#if members && members.length > 0}
						<Card class="max-w-full border-none shadow-none">
							{#each members as member (member.id)}
								<div
									class="flex items-center justify-between border-b-1 border-b-gray-400 text-gray-700"
								>
									<p>{member.username}</p>
									<p>{getLevelText(member.level)}</p>
									<p>{member.status}</p>
									<p>{formatDate(member.joined_at)}</p>
									{#if member.left_at}
										<p>{member.left_at}</p>
									{/if}
								</div>
							{/each}
						</Card>
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
								{formatDate(project?.start_date || '')}
							</div>
							<div class="text-xs text-gray-500">
								{formatRelativeDate(project?.start_date || '')}
							</div>
						</div>
					</div>

					<div class="flex items-center justify-between text-sm">
						<span class="text-gray-500 dark:text-gray-400">End Date</span>
						<div class="text-right">
							<div class="font-medium text-gray-900 dark:text-white">
								{formatDate(project?.end_date || '')}
							</div>
							<div class="text-xs text-gray-500">
								{formatRelativeDate(project?.end_date || '')}
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
								<span class="text-gray-500">Completed</span>
								<span class="font-medium text-green-600">0</span>
							</div>
							<div class="flex justify-between text-sm">
								<span class="text-gray-500">In Progress</span>
								<span class="font-medium text-blue-600">0</span>
							</div>
							<div class="flex justify-between text-sm">
								<span class="text-gray-500">Pending</span>
								<span class="font-medium text-yellow-600">0</span>
							</div>
						</div>
					</Card>
				</div>

				<div>
					<Card class="p-4">
						<h3 class="mb-3 font-semibold text-gray-900 dark:text-white">Actions</h3>
						<div class="space-y-2">
							<Button
								class="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700"
								disabled={isArchived}
							>
								Edit Project
							</Button>
							<Button
								class="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700"
								onclick={() => {
									modal = true;
								}}
								disabled={isArchived}
							>
								Add Task
							</Button>
							<Button
								class="w-full rounded-lg border border-red-300 bg-white px-3 py-2 text-sm font-medium text-red-700 hover:bg-red-50 dark:border-red-600 dark:bg-gray-800 dark:text-red-400 dark:hover:bg-red-900/20"
								onclick={() => {
									modalArchive = true;
								}}
							>
								{isArchived ? 'Active' : 'Archive'}
							</Button>
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
				<Card
					class="h-60 w-full max-w-sm  transition-all duration-300 hover:scale-105 hover:shadow-2xl"
				>
					<div class="flex h-full flex-col p-4">
						<div class="mb-4 flex justify-between">
							<h1
								class="line-clamp-2 text-xl leading-tight font-bold text-black transition-colors duration-200 hover:text-blue-300"
							>
								<a
									href="/task/{task.id}"
									class="hover:underline"
									data-sveltekit-preload-data="hover">{task.title}</a
								>
							</h1>
							<span
								class="rounded-full border px-3 py-1 text-xs font-semibold {getPriorityStyle(
									task.status
								)}"
							>
								{getPriorityText(task.priority)}
							</span>
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
									datetime={task.due_date}
								>
									📅 {formatDate(task.due_date)}
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
