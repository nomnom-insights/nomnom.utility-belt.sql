-- :name setup* :!
create extension if not exists "hstore";
create extension if not exists "uuid-ossp";

create table if not exists people (
id serial primary key,
name text not null,
email text not null,
entity_id uuid default uuid_generate_v4(),
attributes jsonb,
created_at timestamp default current_timestamp,
updated_at timestamp default current_timestamp,
confirmed_at timestamp
)

-- :name get-all* :? :*
select name, email, attributes, confirmed_at, entity_id from people

--~ (when (:email params) "where email = :email")
order by id desc;

-- :name count-all* :? :1
select count(*) from people

-- :name add* :<!
insert into people
( name, email, attributes, updated_at, confirmed_at, entity_id)
values
(:name, :email, CAST(:attributes AS jsonb), current_timestamp, :confirmed-at, :entity-id)
returning name, email, attributes, confirmed_at, entity_id

-- :name set-email* :! :n
update people
set email = :new-email
where email = :old-email


-- :name delete* :! :n
delete from people
where email = :email


-- :name delete-all* :! :*
truncate people
