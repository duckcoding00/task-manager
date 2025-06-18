<script lang="ts">
	import { enhance } from '$app/forms';
	import type { SubmitFunction } from '@sveltejs/kit';
	import { Button, Modal, type ModalProps } from 'flowbite-svelte';
	import { slide } from 'svelte/transition';
	type Props = ModalProps & {
		open?: boolean;
		loading?: boolean;
		question?: string;
		enhance?: SubmitFunction;
		action?: string;
		id?: string;
		isDelete?: boolean;
		status?: string;
	};

	let {
		open = $bindable(false),
		loading,
		question,
		id,
		isDelete = false,
		status,
		enhance: enhanceFunction,
		action,
		...restProps
	}: Props = $props();
</script>

<Modal
	class="w-full"
	size="sm"
	autoclose={false}
	bind:open
	transition={slide}
	params={{ delay: 150, duration: 200 }}
	{...restProps}
>
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
		{question}
	</p>
	<div class="flex items-center justify-center space-x-4">
		<Button color="light" onclick={() => (open = false)}>No, Cancel</Button>
		<form {action} method="post" use:enhance={enhanceFunction}>
			<input type="hidden" name="id" value={id} />
			<input type="hidden" name="status" value={status} />
			<Button color={isDelete ? 'red' : 'alternative'} type="submit" disabled={loading}>
				<svg class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
					></path>
				</svg>
				{loading ? 'Redirecting....' : `Yes, I'm sure`}
			</Button>
		</form>
	</div>
</Modal>
