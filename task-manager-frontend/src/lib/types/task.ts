export type TaskStatus = 'todo' | 'in_progress' | 'completed' | 'canceled';
export type ProjectStatus = 'active' | 'inactive' | 'archive' | 'cancel';
export type TaskPriority = 'low' | 'medium' | 'high';
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

export interface projects {
	id: number;
	name: string;
	description: string;
	status: ProjectStatus;
	start_date: string;
	end_date: string;
}

export interface project {
	id: number;
	name: string;
	description: string;
	status: string;
	createdBy: string;
	start_date: string;
	end_date: string;
	created_at?: string;
	updated_at?: string;
}

export interface tasks {
	id: number;
	title: string;
	description: string;
	status: string;
	priority: string;
	due_date: string;
}

export interface projectWithData {
	project: project;
	tasks: tasks[];
}
export interface ApiResponse<T> {
	status_code: number;
	message: string;
	timestamp: string;
	data: T;
}

export type GetProjectsResponse = ApiResponse<projects[]>;
export type GetProjecTaskResponse = ApiResponse<projectWithData>;
