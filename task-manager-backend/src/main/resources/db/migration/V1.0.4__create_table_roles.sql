create table if not exists roles(
    id serial primary key,
    name varchar(50) not null unique,
    level integer not null,
    description text
);

INSERT INTO roles (name, level, description) VALUES 
('owner', 3, 'Project owner with full access'),
('admin', 2, 'Project admin with management access'),
('member', 1, 'Project member with basic access');