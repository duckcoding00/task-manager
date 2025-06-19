<script lang="ts">
	interface Props {
		username: string;
		level: number;
		status: string;
	}

	let { username, level, status }: Props = $props();

	const levelConfig = {
		3: { bg: 'bg-violet-600', initial: 'OW', label: 'Owner' },
		2: { bg: 'bg-blue-600', initial: 'AD', label: 'Admin' },
		1: { bg: 'bg-amber-500', initial: 'ME', label: 'Member' }
	} as const;

	const statusConfig = {
		active: 'bg-emerald-500',
		inactive: 'bg-slate-400',
		left: 'bg-slate-500'
	} as const;

	const levelInfo = $derived(levelConfig[level as keyof typeof levelConfig]);
	const statusColor = $derived(statusConfig[status as keyof typeof statusConfig]);
</script>

<div class="inline-flex items-center overflow-hidden rounded-full border border-slate-200 bg-white">
	<div class="px-3 py-1.5 text-sm font-medium text-slate-700">
		{username}
	</div>
	<div class="flex items-center gap-1.5 px-2.5 py-1.5 {levelInfo?.bg} text-white">
		<div class="ml-1 flex h-5 w-5 items-center justify-center rounded-full text-xs font-semibold">
			{levelInfo?.label}
		</div>
		<div class="m-1 h-2 w-2 rounded-full {statusColor}"></div>
	</div>
</div>
