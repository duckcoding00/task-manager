<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { LayoutData } from './$types';
	import { page } from '$app/state';
	import {
		Alert,
		Button,
		Card,
		Datepicker,
		Input,
		Label,
		Li,
		List,
		Modal,
		Textarea
	} from 'flowbite-svelte';
	import { slide } from 'svelte/transition';
	import { formatDate, formatToDateString } from '$lib/utils/formatDate';
	import { goto, invalidateAll } from '$app/navigation';
	import { enhance } from '$app/forms';

	let { data, children }: { data: LayoutData; children: Snippet } = $props();

	// Form state
	let modal = $state(false);
	let name = $state('');
	let description = $state('');
	let loading = $state(false);
	let timeline: { from: Date | undefined; to: Date | undefined } = $state({
		from: undefined,
		to: undefined
	});
	let errorMessages = $state<string[]>([]);
	let successMessage = $state('');

	// Validation state
	let nameError = $state('');
	let dateError = $state('');

	function resetForm() {
		name = '';
		description = '';
		timeline.from = undefined;
		timeline.to = undefined;
		nameError = '';
		dateError = '';
	}

	function handleCancel() {
		resetForm();
		modal = false;
	}

	function customEnhance() {
		loading = true;
		errorMessages = [];
		successMessage = '';

		return async ({ result, update }: any) => {
			loading = false;
			console.log(result);

			if (result.type === 'success') {
				successMessage = 'Success created new project, redirecting...';
				resetForm();
				await invalidateAll();

				setTimeout(() => {
					modal = false;
					successMessage = '';
					location.reload();
					goto('/teams');
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
</script>

<div class="m-4 p-4">
	{@render children()}

	{#if page.url.pathname === '/teams'}
		<div class="fixed right-6 bottom-6 z-50">
			<Button
				class="group h-14 w-14 rounded-full bg-gray-600 text-white shadow-lg transition-all duration-300 hover:scale-110 hover:bg-gray-700 hover:shadow-xl focus:ring-4 focus:ring-gray-300"
				onclick={() => {
					modal = true;
				}}
				aria-label="Create new project"
			>
				<svg
					class="h-6 w-6 transition-transform group-hover:rotate-90"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"
					></path>
				</svg>
			</Button>
		</div>
	{/if}

	<!-- Modal -->
	<Modal
		bind:open={modal}
		autoclose={false}
		size="md"
		class="w-full"
		outsideclose={!loading}
		transition={slide}
	>
		<div class="flex flex-col">
			<div class="mb-6 border-b border-gray-200 pb-4 dark:border-gray-600">
				<h3 class="text-xl font-semibold text-gray-900 dark:text-white">Create New Project</h3>
				<p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
					Set up a new team project with timeline and description
				</p>
			</div>

			<form class="space-y-6" action="?/create" method="POST" use:enhance={customEnhance}>
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
						id="project-name"
						name="name"
						bind:value={name}
						type="text"
						placeholder="Enter project name..."
						color={nameError ? 'red' : 'gray'}
						required
						disabled={loading}
						class="transition-colors focus:ring-2"
						autocomplete="off"
					/>
					<!-- {#if nameError}
						<p class="text-sm text-red-600 dark:text-red-400" transition:slide={{ duration: 200 }}>
							{nameError}
						</p>
					{/if} -->
				</div>

				<div class="space-y-2">
					<Label
						for="project-description"
						class="text-sm font-medium text-gray-900 dark:text-white"
					>
						Description
					</Label>
					<Textarea
						id="project-description"
						name="description"
						bind:value={description}
						placeholder="Describe what this project is about..."
						color="gray"
						rows={3}
						disabled={loading}
						class="resize-none transition-colors focus:ring-2"
					/>
					<p class="text-xs text-gray-500 dark:text-gray-400">
						Optional: Help your team understand the project goals
					</p>
				</div>

				<div class="space-y-2">
					<Label class="text-sm font-medium text-gray-900 dark:text-white">
						Project Timeline *
					</Label>
					<Datepicker
						range
						bind:rangeFrom={timeline.from}
						bind:rangeTo={timeline.to}
						color={dateError ? 'red' : 'gray'}
						disabled={loading}
						placeholder="Select start and end dates"
					/>
					<!-- {#if dateError}
						<p class="text-sm text-red-600 dark:text-red-400" transition:slide={{ duration: 200 }}>
							{dateError}
						</p>
					{/if} -->
					{#if timeline.from && timeline.to}
						<p
							class="text-xs text-gray-600 dark:text-gray-300"
							transition:slide={{ duration: 200 }}
						>
							Duration: {Math.ceil(
								(timeline.to.getTime() - timeline.from.getTime()) / (1000 * 60 * 60 * 24)
							)} days
						</p>
					{/if}
					<input
						type="hidden"
						name="start_date"
						id="start_date"
						value={timeline.from ? formatToDateString(timeline.from.toDateString()) : ''}
					/>
					<input
						type="hidden"
						name="end_date"
						id="end_date"
						value={timeline.to ? formatToDateString(timeline.to.toDateString()) : ''}
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
							Creating Project...
						{:else}
							<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M12 4v16m8-8H4"
								></path>
							</svg>
							Create Project
						{/if}
					</Button>

					<Button
						type="button"
						color="alternative"
						onclick={handleCancel}
						disabled={loading}
						class="flex-1 sm:w-auto sm:flex-none"
					>
						Cancel
					</Button>
				</div>
			</form>
		</div>
	</Modal>
</div>
