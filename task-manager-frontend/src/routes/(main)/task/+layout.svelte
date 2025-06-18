<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { LayoutData } from './$types';
	import { Button } from 'flowbite-svelte';
	import { goto, invalidateAll } from '$app/navigation';
	import { page } from '$app/state';
	import FormModal from '$lib/components/FormModal.svelte';

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

	<FormModal
		bind:open={modal}
		action="?/create&redirectTo={page.url.pathname}"
		size="md"
		formTitle="Create Task"
		formDescription="Set up a new task with due date and description"
		{errorMessages}
		{successMessage}
		first_field="Title"
		first_field_value={title}
		second_field="Description"
		second_field_value={description}
		{loading}
		enhance={customEnhance}
		datepicker
		third_field="Due Date Task"
		formDate={date}
	/>
{:else}
	{@render children()}
{/if}
