<script lang="ts">
	import { Button, Card } from 'flowbite-svelte';
	import type { PageData } from './$types';

	let { data }: { data: PageData } = $props();

	let task = $state(data.task);

	function formatDate(dateString: string | undefined): string {
		if (!dateString) return 'N/A';
		return new Date(dateString).toLocaleString('en-US', {
			month: 'short',
			day: 'numeric',
			hour: 'numeric',
			minute: '2-digit',
			hour12: true
		});
	}

	function getStatusStyle(status: string) {
		switch (status) {
			case 'todo':
				return 'bg-red-100 text-red-800 border-red-200';
			case 'in_progress':
				return 'bg-yellow-100 text-yellow-800 border-yellow-200';
			case 'completed':
				return 'bg-green-100 text-green-800 border-green-200';
			case 'canceled':
				return 'bg-red-400 text-red-800 border-red-600';
			default:
				return 'bg-gray-100 text-gray-800 border-gray-200';
		}
	}

	function getStatusLabel(status: string | undefined) {
		return status ? status.replace('_', ' ').toUpperCase() : '';
	}
</script>

<div class="m-4 flex items-center justify-center p-4">
	<Card
		class="h-100 w-full max-w-xl border-slate-700 bg-gradient-to-br from-slate-800 to-slate-900 shadow-xl transition-all duration-300 hover:scale-105 hover:shadow-2xl md:h-120 md:max-w-2xl"
	>
		<div class="m-4 flex h-full flex-col items-start pb-4">
			<div class="mb-6 flex w-full items-center justify-between">
				<h1
					class="mr-4 line-clamp-2 flex-1 text-xl leading-tight font-bold text-white transition-colors duration-200 hover:text-blue-300"
				>
					{task?.title}
				</h1>
				<span
					class="flex-shrink-0 rounded-full border px-3 py-1 text-xs font-semibold {getStatusStyle(
						task?.status || ''
					)}"
				>
					{getStatusLabel(task?.status)}
				</span>
			</div>

			<div class="mb-4 flex-1">
				<p class="line-clamp-4 text-sm leading-relaxed text-slate-300">
					{task?.description}
				</p>
			</div>

			<div class="flex-grow"></div>

			<div class="mt-auto w-full">
				<!-- Timeline Section -->
				<div class="mb-6 space-y-3">
					<h3 class="text-sm font-semibold text-slate-200 mb-3">Timeline</h3>

					<div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
						<!-- Created -->
						<div class="rounded-lg bg-slate-800/40 p-3 border border-slate-700/50">
							<div class="flex items-center gap-2 mb-1">
								<span class="text-sm">🎯</span>
								<span class="text-xs font-medium text-slate-400 uppercase tracking-wide"
									>Created</span
								>
							</div>
							<time
								class="text-sm text-slate-300 font-medium"
								datetime={task?.created_at}
							>
								{formatDate(task?.created_at)}
							</time>
						</div>

						<!-- Updated -->
						<div class="rounded-lg bg-slate-800/40 p-3 border border-slate-700/50">
							<div class="flex items-center gap-2 mb-1">
								<span class="text-sm">🔄</span>
								<span class="text-xs font-medium text-slate-400 uppercase tracking-wide"
									>Updated</span
								>
							</div>
							<time
								class="text-sm text-slate-300 font-medium"
								datetime={task?.updated_at}
							>
								{formatDate(task?.updated_at)}
							</time>
						</div>

						<!-- Due Date -->
						<div class="rounded-lg bg-slate-800/40 p-3 border border-slate-700/50">
							<div class="flex items-center gap-2 mb-1">
								<span class="text-sm">📅</span>
								<span class="text-xs font-medium text-slate-400 uppercase tracking-wide"
									>Due Date</span
								>
							</div>
							<time
								class="text-sm text-slate-300 font-medium"
								datetime={task?.due_dated_at}
							>
								{formatDate(task?.due_dated_at)}
							</time>
						</div>
					</div>
				</div>

				<!-- Action Buttons -->
				<div class="border-t border-slate-700/50 pt-4">
					<div class="flex flex-wrap gap-3 justify-end">
						<Button
							color="light"
							class="bg-slate-700/50 text-slate-300 border-slate-600 hover:bg-slate-600/50 hover:text-white transition-all duration-200"
						>
							<svg
								class="w-4 h-4 mr-2"
								fill="none"
								stroke="currentColor"
								viewBox="0 0 24 24"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
								></path>
							</svg>
							Edit
						</Button>

						<Button
							color="red"
							class="bg-red-600/20 text-red-400 border-red-500/30 hover:bg-red-600/30 hover:text-red-300 transition-all duration-200"
						>
							<svg
								class="w-4 h-4 mr-2"
								fill="none"
								stroke="currentColor"
								viewBox="0 0 24 24"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
								></path>
							</svg>
							Delete
						</Button>
					</div>
				</div>
			</div>
		</div>
	</Card>
</div>
