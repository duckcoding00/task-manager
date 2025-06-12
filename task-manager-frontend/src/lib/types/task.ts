export type TaskStatus = 'todo' | 'in_progress' | 'completed' | 'canceled';

export interface Task {
	id?: number;
	user_id?: number;
	title: string;
	description: string;
	status: TaskStatus;
	due_dated_at: string; // ISO 8601 date string
	created_at?: string;
	updated_at?: string;
}

export interface TaskResponse {
	data: Task[];
	message?: string;
}

export interface SingleTaskResponse {
	data: Task;
	message?: string;
}

export interface TaskErrorResponse {
	errors: string[];
}

export interface ErrorResponse {
	status_code: number;
	errors: string;
	timestamp: string;
	details: {
		error: string;
	};
}
