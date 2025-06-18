<script lang="ts">
	import { enhance } from '$app/forms';
	import { page } from '$app/state';
	import { formatToDateString, formatToISO } from '$lib/utils/formatDate';
	import type { error, SubmitFunction } from '@sveltejs/kit';
	import {
		Card,
		Alert,
		List,
		Li,
		Modal,
		Label,
		Input,
		Button,
		Datepicker,
		Select
	} from 'flowbite-svelte';
	import type { ModalProps } from 'flowbite-svelte';
	import { slide } from 'svelte/transition';

	let selected = $state();
	let priority = [
		{ value: 'low', name: 'Low' },
		{ value: 'medium', name: 'Medium' },
		{ value: 'high', name: 'high' }
	];

	type props = ModalProps & {
		open?: boolean;
		size?: 'sm' | 'md' | 'lg';

		formTitle?: string;
		formDescription?: string;
		action?: string;
		errorMessages?: string[];
		successMessage?: string;
		first_field?: string;
		first_field_value?: any;
		second_field?: string;
		second_field_value?: any;
		third_field?: string;
		third_field_value?: any;

		datepicker?: boolean;
		update?: boolean;
		isSelected?: boolean;
		formDate?: Date | undefined;
		toDate?: Date | undefined;

		hidden_field?: string;
		hidden_field_value?: any;
		loading?: boolean;
		enhance?: SubmitFunction;
	};

	let {
		open = $bindable(false),
		size = 'sm',

		formTitle,
		formDescription,
		action,
		errorMessages,
		successMessage,
		first_field,
		first_field_value,
		second_field,
		second_field_value,
		third_field,
		third_field_value,

		datepicker = false,
		update = false,
		isSelected = false,
		formDate = undefined,
		toDate = undefined,

		hidden_field,
		hidden_field_value,
		loading,
		enhance: enhanceFunction,
		...restProps
	}: props = $props();

	function handleCancel() {
		first_field_value = '';
		second_field_value = '';
		errorMessages = [];
		successMessage = '';
		formDate = undefined;
		toDate = undefined;
		closeModal();
	}

	function closeModal() {
		open = false;
	}

	function toDateString(date: string) {
		if (page.url.pathname.includes('/teams')) {
			return formatToDateString(date || '');
		}

		return formatToISO(date || '');
	}
</script>

<Modal
	class="w-full"
	bind:open
	autoclose={false}
	transition={slide}
	params={{ delay: 150, duration: 200 }}
	{size}
	{...restProps}
>
	<div class="flex flex-col">
		<div class="mb-6 border-b border-b-gray-200 pb-4">
			<h3 class="text-xl font-semibold text-gray-900">{formTitle}</h3>
			<p class="mt-1 text-sm text-gray-400">{formDescription}</p>
		</div>

		<form class="mx-4 space-y-6" {action} method="POST" use:enhance={enhanceFunction}>
			{@render message({ error: errorMessages, success: successMessage })}

			<div class="space-y-2">
				<Label for={first_field?.toLowerCase()} class="text-sm font-medium text-gray-800"
					>{first_field} <span class=" text-red-500">*</span></Label
				>
				<Input
					class="transition-colors focus:ring-2"
					id={first_field?.toLowerCase()}
					name={first_field?.toLowerCase()}
					bind:value={first_field_value}
					type="text"
					placeholder="Enter {first_field}...."
					color="gray"
					required
					disabled={loading}
					autocomplete="off"
				/>
			</div>

			<div class="space-y-2">
				<Label for={second_field?.toLowerCase()} class="text-sm font-medium text-gray-800"
					>{second_field} <span class=" text-red-500">*</span></Label
				>
				<Input
					class="transition-colors focus:ring-2"
					id={second_field?.toLowerCase()}
					name={second_field?.toLowerCase()}
					bind:value={second_field_value}
					type="text"
					placeholder="Enter {second_field}...."
					color="gray"
					required
					disabled={loading}
					autocomplete="off"
				/>
			</div>

			{#if hidden_field}
				<input type="hidden" name={hidden_field} value={hidden_field_value} />
			{/if}

			{#if isSelected}
				<div class="space-y-2">
					<Label class="text-sm font-medium text-gray-900 dark:text-white">Priority Task</Label>
					<Label>
						<Select class="mt-2" items={priority} bind:value={selected} required name="priority" />
					</Label>
				</div>
			{/if}

			<div class="space-y-2">
				{#if datepicker}
					<Label for={third_field?.toLowerCase()} class="text-sm font-medium text-gray-800"
						>{third_field} <span class=" text-red-500">*</span></Label
					>
					{#if toDate}
						<Datepicker
							range
							bind:rangeFrom={formDate}
							bind:rangeTo={toDate}
							disabled={loading}
							placeholder="Select start and end dates"
						/>

						<input
							type="hidden"
							name="start_date"
							id="start_date"
							value={formDate ? formatToDateString(formDate.toDateString()) : ''}
						/>
						<input
							type="hidden"
							name="end_date"
							id="end_date"
							value={toDate ? formatToDateString(toDate.toDateString()) : ''}
						/>
					{:else}
						<Datepicker bind:value={formDate} color="gray" id="date" />
						<input
							type="hidden"
							name="date"
							id="date"
							value={toDateString(formDate?.toDateString() || '')}
						/>
					{/if}
				{/if}
			</div>

			<div class="flex flex-col gap-3 pt-4 sm:flex-row-reverse">
				<Button
					type="submit"
					color="gray"
					disabled={loading}
					class="flex-1 bg-gray-700 text-white hover:bg-gray-800 disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto sm:flex-none"
					>{#if loading}
						Redirecting...
					{:else}
						{update ? 'Update' : 'Create'}
					{/if}</Button
				>
				<Button
					type="button"
					color="alternative"
					onclick={update ? closeModal : handleCancel}
					disabled={loading}
					class="flex-1 sm:w-auto sm:flex-none"
				>
					Cancel
				</Button>
			</div>
		</form>
	</div>
</Modal>

{#snippet message({
	error,
	success
}: {
	error: typeof errorMessages;
	success: typeof successMessage;
})}
	{#if error && error.length > 0}
		<Alert color="red" transition={slide} params={{ delay: 100, duration: 200 }}>
			<List>
				{#each errorMessages || [] as error}
					<Li>{error}</Li>
				{/each}
			</List>
		</Alert>
	{/if}

	{#if success}
		<Alert color="green">
			<span>{successMessage}</span>
		</Alert>
	{/if}
{/snippet}

{#snippet fields()}{/snippet}
