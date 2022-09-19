create table cities (id bigint not null, country varchar(255), filename varchar(255), title varchar(255) not null, primary key (id));
create table user_role (user_id bigint not null, roles varchar(255));
create table users (id bigint not null, activation_code varchar(255), active bit not null, email varchar(255), password varchar(255) not null, record integer, username varchar(255) not null, primary key (id));
alter table user_role add constraint user_role_user_fk foreign key (user_id) references users (id);