-- :name setup* :!
create table if not exists people (
id serial primary key,
name text not null,
email text not null,
attributes jsonb
)

-- :name get-all* :? :*
select name, email, attributes from people

--~ (when (:email params) "where email = :email")
order by id desc;

-- :name add* :<!
insert into people
( name, email, attributes)
values
(:name, :email, CAST(:attributes AS jsonb))
returning name, email, attributes

-- :name delete* :! :1
delete from people
where email = :email

-- :name delete-all* :! :*
truncate people
