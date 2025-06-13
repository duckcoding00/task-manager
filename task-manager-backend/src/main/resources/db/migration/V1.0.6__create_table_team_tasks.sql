create type priority_task as enum('low', 'medium', 'high');

create table if not exists team_tasks(
    id serial primary key,
    project_id integer not null,
    title varchar(255) not null,
    description text,
    status task_status not null default 'todo',
    priority priority_task not null default 'medium',
    due_date date,
    created_by varchar(255) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    constraint fk_team_tasks_project foreign key (project_id) references projects(id),
    constraint fk_team_tasks_creator foreign key (created_by) references users(username),
    is_deleted boolean default false,
    deleted_at timestamp
);