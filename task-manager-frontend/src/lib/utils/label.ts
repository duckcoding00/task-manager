export function getStatusClass(status: string) {
	switch (status) {
		case 'active':
			return 'bg-green-100 text-green-800 border border-green-200';
		case 'inactive':
			return 'bg-gray-100 text-gray-800 border border-gray-200';
		case 'archive':
			return 'bg-blue-100 text-blue-800 border border-blue-200';
		case 'cancel':
			return 'bg-red-100 text-red-800 border border-red-200';
		default:
			return 'bg-gray-100 text-gray-800 border border-gray-200';
	}
}

export function getStatusText(status: string) {
	switch (status) {
		case 'active':
			return 'Active';
		case 'inactive':
			return 'Inactive';
		case 'archive':
			return 'Archived';
		case 'cancel':
			return 'Cancelled';
		default:
			return status.charAt(0).toUpperCase() + status.slice(1);
	}
}

export function getStatusStyle(status: string) {
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

export function getStatusLabel(status: string) {
	return status.replace('_', ' ').toUpperCase();
}

export function getPriorityText(status: string) {
	switch (status) {
		case 'low':
			return 'Low';
		case 'medium':
			return 'Medium';
		case 'high':
			return 'High';
		case 'urgent':
			return 'Urgent';
		default:
			return status.charAt(0).toUpperCase() + status.slice(1);
	}
}

// Perbaikan untuk getPriorityStyle - mengembalikan CSS classes
export function getPriorityStyle(priority: string) {
	switch (priority) {
		case 'low':
			return 'bg-blue-100 text-blue-800 border border-blue-200';
		case 'medium':
			return 'bg-yellow-100 text-yellow-800 border border-yellow-200';
		case 'high':
			return 'bg-orange-100 text-orange-800 border border-orange-200';
		case 'urgent':
			return 'bg-red-100 text-red-800 border border-red-200';
		default:
			return 'bg-gray-100 text-gray-800 border border-gray-200';
	}
}

// Tambahan: Function untuk mendapatkan icon priority
export function getPriorityIcon(priority: string) {
	switch (priority) {
		case 'low':
			return '⬇️';
		case 'medium':
			return '➡️';
		case 'high':
			return '⬆️';
		case 'urgent':
			return '🔥';
		default:
			return '❓';
	}
}
