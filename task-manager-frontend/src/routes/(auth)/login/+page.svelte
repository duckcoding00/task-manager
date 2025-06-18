<script lang="ts">
	// import type { ActionData, PageData } from './$types';
	import { Input, Button, Checkbox, Card, Alert, List, Li } from 'flowbite-svelte';
	import { enhance } from '$app/forms';
	import { page } from '$app/state';
	import { goto, invalidateAll, preloadData } from '$app/navigation';

	// let { data, form }: { data: PageData; form: ActionData } = $props();
	let loading = $state(false);
	let errorMessage = $state('');
	let errorMessages = $state<string[]>([]);

	function delay(ms: number) {
		return new Promise((resolve) => setTimeout(resolve, ms));
	}

	function customEnhance() {
		loading = true;
		errorMessage = '';
		errorMessages = [];

		return async ({ result, update }: any) => {
			console.log(result);

			try {
				if (result.type === 'redirect') {
					await delay(500);
					await preloadData(result.location);
					goto(result.location);
					return;
				}

				if (result.type === 'success') {
					await invalidateAll();
					await update();
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
			} catch (error) {
				console.error('Error in deleteEnhance:', error);
			} finally {
				loading = false;
			}
		};
	}
</script>

<div class="flex min-h-screen flex-col items-center justify-center md:flex-row md:px-4 md:py-4">
	<Card
		size="lg"
		class="order-2 hidden min-h-screen w-full flex-none rounded-none border-none bg-[#1e2838] text-center text-white shadow-none md:order-2 md:block md:w-1/3 md:rounded-r-lg"
	>
		<div class="flex min-h-screen items-center justify-center">
			<h1>Lets join with us</h1>
		</div>
	</Card>

	<Card
		size="lg"
		class="order-1 min-h-screen w-full flex-1 rounded-none text-center text-white shadow-none md:order-1 md:h-screen md:min-h-0 md:w-2/3 md:rounded-l-lg"
	>
		<div class="flex min-h-screen items-center justify-center">
			<Card
				class="w-full max-w-md rounded-none border-none bg-transparent p-4 shadow-none sm:p-6 md:p-8"
			>
				<form
					class="flex flex-col space-y-6"
					action="?/login&redirectTo={page.url.pathname}"
					method="POST"
					use:enhance={customEnhance}
				>
					<h3 class="text-center text-xl font-bold text-gray-950 dark:text-white">Login Account</h3>

					{#if errorMessage}
						<Alert class="mb-4 bg-[#1e2838]">
							<span class="font-medium text-white">Error! {errorMessage}</span>
						</Alert>
					{/if}

					{#if errorMessages.length > 0}
						<Alert class="mb-4 bg-[#1e2838] text-left">
							<List>
								{#each errorMessages as error}
									<Li class="text-sm text-white">{error}</Li>
								{/each}
							</List>
						</Alert>
					{/if}

					<label for="username" class="space-y-2">
						<Input
							type="text"
							name="username"
							placeholder="username"
							required
							id="username"
							color="gray"
						/>
					</label>
					<label for="password" class="space-y-2">
						<Input
							type="password"
							name="password"
							placeholder="password"
							required
							id="password"
							color="gray"
						/>
					</label>
					<div class="flex items-start">
						<Checkbox class="text-[#1e2838]"
							><span class="text-[#1e2838]">Remember me</span></Checkbox
						>
						<a href="/" class="ms-auto text-sm text-gray-700 hover:underline dark:text-white"
							>Lost password?</a
						>
					</div>
					<Button type="submit" class="w-full" color="dark"
						>{loading ? 'Redirecting...' : 'Login'}</Button
					>
					<div class="text-center text-sm font-medium text-gray-500 dark:text-gray-300">
						have account? <a href="/register" class="text-gray-700 hover:underline dark:text-white"
							>create account</a
						>
					</div>
				</form>
			</Card>
		</div>
	</Card>
</div>
