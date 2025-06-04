<script lang="ts">
	import type { ActionData, PageData } from './$types';
	import { Input, Label, Helper, Button, Checkbox, A } from 'flowbite-svelte';

	let { data, form }: { data: PageData; form: ActionData } = $props();
</script>

<h1>Login Page</h1>

<form action="?/login" method="POST">
	<div class="mb-6">
		<Label for="username" class="mb-2">username</Label>
		<Input
			type="username"
			id="username"
			placeholder="john.doe@company.com"
			required
			name="username"
			value={form?.username?.toString() || ''}
		/>
	</div>
	<div class="mb-6">
		<Label for="password" class="mb-2">Password</Label>
		<Input
			type="password"
			id="password"
			placeholder="•••••••••"
			required
			name="password"
			value={form?.password?.toString() || ''}
		/>
	</div>
	<Button type="submit">Submit</Button>
</form>

{#if form?.passwordErrs}
	<p>{form.passwordErrs}</p>
{:else if form?.usernameErrs}
	<p>{form.usernameErrs}</p>
{/if}
