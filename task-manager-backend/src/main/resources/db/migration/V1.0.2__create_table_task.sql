create type task_status as enum ('todo', 'in_progress', 'completed', 'canceled');

create table if not exists tasks(
    id serial primary key,
    user_id int not null,
    title varchar(255) not null,
    description text not null,
    status task_status not null default 'todo',
    created_at timestamp with time zone default current_timestamp,
    updated_at timestamp with time zone default current_timestamp,
    duedated_at timestamp with time zone default current_timestamp,
    constraint fk_tasks_user_id foreign key (user_id) references users(id)
);