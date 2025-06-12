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
				class="h-14 w-14 rounded-full bg-gray-600 text-white shadow-lg transition-all duration-300 hover:scale-110 hover:bg-gray-700 hover:shadow-xl"
				onclick={() => {
					modal = true;
				}}
			>
				<svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"
					></path>
				</svg>
			</Button>
		</div>
	{/if}

	<Modal bind:open={modal} autoclose={false} size="md" transition={slide}>
		<div class="flex justify-center">
			<Card class="max-w-full rounded-none border-none p-4 shadow-none sm:p-6 md:p-8">
				<form
					class="flex flex-col space-y-6"
					action="?/create&redirectTo={page.url.pathname}"
					method="POST"
					use:enhance={customEnhance}
				>
					<h3 class="text-xl font-bold text-gray-900 dark:text-white">Create New Task</h3>

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

					<Label for="title">
						<span>Title</span>
						<Input
							bind:value={title}
							type="text"
							name="title"
							placeholder="Task title"
							id="title"
							color="gray"
							required
							disabled={loading}
						/>
					</Label>

					<Label for="description">
						<span>Description</span>
						<Textarea
							bind:value={description}
							id="description"
							placeholder="Task description"
							color="gray"
							rows={4}
							name="description"
							disabled={loading}
						/>
					</Label>

					<label for="date">
						<span>Date</span>
						<Datepicker bind:value={date} color="gray" id="date" />
						<input
							type="hidden"
							name="date"
							id="date"
							value={date ? formatToISO(date.toString()) : ''}
						/>
					</label>

					<div class="flex gap-3">
						<Button
							type="submit"
							class="flex-1 bg-gray-700 text-white hover:bg-gray-900"
							disabled={loading}
						>
							{loading ? 'Creating...' : 'Create Task'}
						</Button>
						<Button color="alternative" onclick={resetForm} disabled={loading}>Cancel</Button>
					</div>
				</form>
			</Card>
		</div>
	</Modal>
{:else}
	{@render children()}
{/if}
