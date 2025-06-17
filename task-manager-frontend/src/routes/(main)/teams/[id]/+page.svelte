<script lang="ts">
	import {
		Alert,
		Breadcrumb,
		BreadcrumbItem,
		Button,
		Card,
		Datepicker,
		Heading,
		Input,
		Label,
		Li,
		List,
		Modal,
		P,
		Select,
		Textarea
	} from 'flowbite-svelte';
	import type { PageData } from './$types';
	import { formatDate, formatToDateString, formatToISO } from '$lib/utils/formatDate';
	import {
		getLevelText,
		getPriorityStyle,
		getPriorityText,
		getStatusClass,
		getStatusLabel,
		getStatusStyle,
		getStatusText
	} from '$lib/utils/label';
	import { goto, invalidateAll } from '$app/navigation';
	import { slide } from 'svelte/transition';
	import { enhance } from '$app/forms';
	import { Section } from 'flowbite-svelte-blocks';

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
	let priority = [
		{ value: 'low', name: 'Low' },
		{ value: 'medium', name: 'Medium' },
		{ value: 'high', name: 'high' }
	];

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
			loading = false;
			console.log('Form result:', result);

			if (result.type === 'success') {
				successMessage = 'Success Created New Task, redirecting....';
				resetForm();
				await invalidateAll();

				setTimeout(() => {
					closeModal();
					successMessage = '';
					location.reload();
					goto('/task');
				}, 1000);
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
			loadingArchive = false;
			if (result.type === 'success') {
				projectStatus = formData.get('status') as string;
				isArchived = projectStatus === 'archive';
				await invalidateAll();
				await update();
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

<Modal bind:open={modal} autoclose={false} size="md" transition={slide}>
	<div class="flex justify-center">
		<Card class="max-w-full rounded-none border-none p-4 shadow-none sm:p-6 md:p-8">
			<div class="mb-6 border-b border-gray-200 pb-4 dark:border-gray-600">
				<h3 class="text-xl font-semibold text-gray-900 dark:text-white">Create New Task</h3>
				<p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
					Set up a new task with timeline and description
				</p>
			</div>
			<form
				class="flex flex-col space-y-6"
				method="POST"
				action="?/create"
				use:enhance={customEnhance}
			>
				{#if errorMessages.length > 0}
					<Alert color="red">
						<List>
							{#each errorMessages as error}
								<Li>{error}</Li>
							{/each}
						</List>
					</Alert>
				{/if}

				{#if successMessage}
					<Alert color="green">
						<span>{successMessage}</span>
					</Alert>
				{/if}

				<div class="space-y-2">
					<Label for="project-name" class="text-sm font-medium text-gray-900 dark:text-white">
						Project Name *
					</Label>
					<Input
						bind:value={title}
						type="text"
						name="title"
						placeholder="Task title...."
						id="title"
						color="gray"
						required
						disabled={loading}
					/>
				</div>
				<div class="space-y-2">
					<Label
						for="project-description"
						class="text-sm font-medium text-gray-900 dark:text-white"
					>
						Description
					</Label>
					<Textarea
						bind:value={description}
						id="description"
						placeholder="Task description...."
						color="gray"
						rows={4}
						name="description"
						disabled={loading}
						class="resize-none transition-colors focus:ring-2"
					/>
					<p class="text-xs text-gray-500 dark:text-gray-400">
						Optional: Help your team understand the project goals
					</p>
				</div>

				<div class="space-y-2">
					<Label class="text-sm font-medium text-gray-900 dark:text-white">Priority Task</Label>
					<Label>
						<Select class="mt-2" items={priority} bind:value={selected} required name="priority" />
					</Label>
				</div>
				<div class="space-y-2">
					<Label class="text-sm font-medium text-gray-900 dark:text-white">
						Project Due Date *
					</Label>
					<Datepicker bind:value={date} color="gray" id="date" />
					<input
						type="hidden"
						name="date"
						id="date"
						value={date ? formatToDateString(date.toString()) : ''}
					/>
				</div>

				<div class="flex flex-col gap-3 pt-4 sm:flex-row-reverse">
					<Button
						type="submit"
						color="gray"
						disabled={loading}
						class="flex-1 bg-gray-700 text-white hover:bg-gray-800 disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto sm:flex-none"
					>
						{#if loading}
							<svg class="mr-2 h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
								<circle
									class="opacity-25"
									cx="12"
									cy="12"
									r="10"
									stroke="currentColor"
									stroke-width="4"
								></circle>
								<path
									class="opacity-75"
									fill="currentColor"
									d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
								></path>
							</svg>
							Creating Task...
						{:else}
							<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M12 4v16m8-8H4"
								></path>
							</svg>
							Create Task
						{/if}
					</Button>

					<Button
						type="button"
						color="alternative"
						onclick={resetForm}
						disabled={loading}
						class="flex-1 sm:w-auto sm:flex-none"
					>
						Cancel
					</Button>
				</div>
			</form>
		</Card>
	</div>
</Modal>

<Section sectionClass="h-96">
	<Modal title="" bind:open={modalArchive} autoclose size="sm" class="w-full">
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
			Are you sure you want to {isArchived ? 'Active' : 'Archive'} this Project?
		</p>
		<div class="flex items-center justify-center space-x-4">
			<Button color="light" onclick={() => (modalArchive = false)}>No, Cancel</Button>
			<form action="?/archive" method="post" use:enhance={archiveEnhanced}>
				<input type="hidden" name="id" value={project?.id} />
				<input type="hidden" name="status" value={isArchived ? 'active' : 'archive'} />
				<Button color={isArchived ? 'green' : 'red'} type="submit">
					<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
						></path>
					</svg>
					Yes, {isArchived ? 'Activated' : 'Archived'} this project
				</Button>
			</form>
		</div>
	</Modal>
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
