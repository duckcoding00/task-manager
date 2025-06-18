<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { LayoutData } from './$types';
	import { page } from '$app/state';
	import { Button } from 'flowbite-svelte';
	import { goto, invalidateAll } from '$app/navigation';
	import FormModal from '$lib/components/FormModal.svelte';
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

	<FormModal
		bind:open={modal}
		action="?/create"
		size="md"
		formTitle="Create Project"
		formDescription="Set up a new team project with timeline and description"
		{errorMessages}
		{successMessage}
		first_field="Name"
		first_field_value={name}
		second_field="Description"
		second_field_value={description}
		{loading}
		enhance={customEnhance}
		datepicker
		third_field="Time Line Project"
		formDate={timeline.from}
		toDate={timeline.to}
	/>
</div>
