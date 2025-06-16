<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { LayoutData } from './$types';
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
	import { goto, invalidateAll } from '$app/navigation';
	import { slide } from 'svelte/transition';
	import { enhance } from '$app/forms';
	import { page, updated } from '$app/state';
	import { formatToISO } from '$lib/utils/formatDate';

	let { data, children }: { data: LayoutData; children: Snippet } = $props();

	let modal = $state(false);
	let title = $state('');
	let description = $state('');
	let dueDate = $state('');
	let date = $state<Date | undefined>(undefined);
	let loading = $state(false);
	let errorMessages = $state<string[]>([]);
	let successMessage = $state('');

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

			if (result.type === 'redirect') {
				closeModal();
				location.reload();
				goto(result.location);
				return;
			}

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
			}

			if (result.type === 'failure') {
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
	}
</script>

{#if !page.error}
	{@render children()}

	{#if page.url.pathname === '/task'}
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
					action="?/create&redirectTo={page.url.pathname}"
					method="POST"
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
						<Label class="text-sm font-medium text-gray-900 dark:text-white">
							Project Timeline *
						</Label>
						<Datepicker bind:value={date} color="gray" id="date" />
						<input
							type="hidden"
							name="date"
							id="date"
							value={date ? formatToISO(date.toString()) : ''}
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
{:else}
	{@render children()}
{/if}
