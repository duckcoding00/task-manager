<script lang="ts">
	import { slide } from 'svelte/transition';

	import {
		Navbar,
		NavBrand,
		NavLi,
		NavUl,
		NavHamburger,
		Button,
		Modal,
		Card,
		Input,
		Checkbox,
		Alert,
		List,
		Li
	} from 'flowbite-svelte';
	import { page } from '$app/state';
	import { enhance } from '$app/forms';
	import { goto, invalidateAll } from '$app/navigation';
	import type { Snippet } from 'svelte';
	import type { LayoutData } from './$types';
	import type { ActionData } from '../(auth)/register/$types';

	let { children, form, data }: { children: Snippet; form: ActionData; data: LayoutData } =
		$props();
	let modal = $state(false);
	let loading = $state(false);
	let errorMessage = $state('');
	let errorMessages = $state<string[]>([]);

	function openModal() {
		modal = true;
		errorMessage = '';
	}

	function closeModal() {
		modal = false;
		errorMessage = '';
		loading = false;
	}

	function customEnhance() {
		loading = true;
		errorMessage = '';
		errorMessages = [];

		return async ({ result, update }: any) => {
			loading = false;
			console.log(result);

			if (result.type === 'redirect') {
				closeModal();
				await invalidateAll();
				goto(result.location);
				return;
			}

			if (result.type === 'success') {
				closeModal();
				await invalidateAll();
				return;
			}

			if (result.type === 'failure') {
				let data = result.data;
				let { validations, message } = data;

				if (validations && Array.isArray(validations)) {
					errorMessages = validations;
					errorMessage = '';
				} else if (message === 'Login failed') {
					errorMessage = message + ' please try again';
					errorMessages = [];
				} else {
					errorMessage = 'An error occurred. Please try again.';
					errorMessages = [];
				}
			}

			await update();
		};
	}
</script>

{#if page.url.pathname !== '/register' && page.url.pathname !== '/login'}
	<Navbar class="bg-neutral-100">
		<NavBrand>
			<a href="/"><span class="text-1xl">Task Manager</span></a>
		</NavBrand>
		<div class="order-2 flex items-center gap-1">
			{#if data.isAuth}
				<form
					action="/?/logout"
					method="POST"
					use:enhance={() => {
						return async ({ result }) => {
							if (result.type === 'redirect') {
								await invalidateAll();
								goto(result.location);
							}
						};
					}}
				>
					<Button type="submit" color="dark" size="sm">Logout</Button>
				</form>
			{:else}
				<Button color="dark" size="sm" onclick={openModal}>Login</Button>
			{/if}

			<NavHamburger />
		</div>

		<NavUl class="order-1">
			<NavLi href="/">Home</NavLi>
			{#if data.isAuth}
				<NavLi href="/task" data-sveltekit-preload-data="hover">Tasks</NavLi>
			{/if}
		</NavUl>
	</Navbar>

	<Modal bind:open={modal} autoclose={false} transition={slide} size="sm">
		<div class="flex justify-center">
			<Card class="w-full rounded-none border-none p-4 shadow-none sm:p-6 md:p-8">
				<form
					class="flex flex-col space-y-6"
					action="/login?/login&redirectTo={page.url.pathname}"
					method="POST"
					use:enhance={customEnhance}
				>
					<div class="flex items-center justify-between">
						<h3 class="text-center text-xl font-bold text-gray-950 dark:text-white">Sign in</h3>
					</div>

					{#if errorMessage}
						<Alert class="mb-4 bg-[#1e2838]">
							<span class="font-medium text-white">Error! {errorMessage}</span>
						</Alert>
					{/if}

					{#if errorMessages.length > 0}
						<Alert class="mb-4 bg-[#1e2838]">
							<List>
								{#each errorMessages as error}
									<Li class="text-sm text-white">{error}</Li>
								{/each}
							</List>
						</Alert>
					{/if}

					<label for="modal-username" class="space-y-2">
						<span>Username</span>
						<Input
							type="text"
							name="username"
							placeholder="username"
							required
							id="modal-username"
							value={form?.username?.toString() || ''}
							disabled={loading}
							color="gray"
						/>
					</label>
					<label for="modal-password" class="space-y-2">
						<span>Password</span>
						<Input
							type="password"
							name="password"
							placeholder="password"
							required
							id="modal-password"
							value={form?.password?.toString() || ''}
							disabled={loading}
							color="gray"
						/>
					</label>
					<div class="flex items-start">
						<Checkbox disabled={loading}>Remember me</Checkbox>
						<a href="/" class="ms-auto text-sm text-gray-700 hover:underline dark:text-white">
							Lost password?
						</a>
					</div>
					<Button type="submit" class="w-full" color="dark" disabled={loading}>
						{loading ? 'Signing in...' : 'Login account'}
					</Button>
					<div class="text-center text-sm font-medium text-gray-500 dark:text-gray-300">
						Not registered?
						<a href="/register" class="text-gray-700 hover:underline dark:text-white"
							>create account</a
						>
					</div>
				</form>
			</Card>
		</div>
	</Modal>

	{@render children()}
{:else}
	{@render children()}
{/if}
