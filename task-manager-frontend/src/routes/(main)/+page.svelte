<script lang="ts">
	import { HeroHeader, Section } from 'flowbite-svelte-blocks';
	import type { PageData } from './$types';
	import { Button } from 'flowbite-svelte';
	import PageTask from '$lib/components/PageTask.svelte';

	let { data }: { data: PageData } = $props();
</script>

{#if data.isAuth}
	<Section name="heroDefault" class="m-auto">
		<HeroHeader>
			{#snippet h1()}
				Hey {data.username}, let's get things done! span🚀
			{/snippet}
			{#snippet paragraph()}
				Your productivity journey continues here. Create tasks, track progress, and celebrate your
				wins along the way.
			{/snippet}
		</HeroHeader>
		<div class="m-10 flex flex-row justify-center space-x-4">
			<a href="/task">
				<Button
					size="lg"
					color="gray"
					class="transition delay-150 duration-300 ease-in-out hover:-translate-y-1 hover:scale-110"
					>Add New Task</Button
				>
			</a>
			<a href="/teams">
				<Button
					size="lg"
					color="light"
					class="transition delay-150 duration-300 ease-in-out hover:-translate-y-1 hover:scale-110"
					>Team Project?</Button
				>
			</a>
		</div>
	</Section>
{:else}
	<Section name="heroDefault" class="m-auto">
		<HeroHeader>
			{#snippet h1()}
				Turn Chaos Into Progress
			{/snippet}
			{#snippet paragraph()}
				Stop juggling tasks in your head. Our smart task manager helps you prioritize, organize, and
				accomplish what matters most.
			{/snippet}
		</HeroHeader>
		<div class="flex flex-row justify-center space-x-4">
			<a href="/register">
				<Button
					size="lg"
					color="gray"
					class="transition delay-150 duration-300 ease-in-out hover:-translate-y-1 hover:scale-110"
					>Start Organizing</Button
				>
			</a>
			<a href="/login">
				<Button
					size="lg"
					color="light"
					class="transition delay-150 duration-300 ease-in-out hover:-translate-y-1 hover:scale-110"
					>Already a Member?</Button
				>
			</a>
		</div>
	</Section>
{/if}
