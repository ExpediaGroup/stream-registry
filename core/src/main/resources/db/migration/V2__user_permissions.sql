
-- Drop table

-- DROP TABLE user_permission;

CREATE TABLE user_permission (
	id int8 NOT NULL,
	permissions varchar(255) NULL,
	principal varchar(255) NULL,
	CONSTRAINT user_permission_pkey PRIMARY KEY (id)
);

-- Drop table

-- DROP TABLE public.stream_acl;

CREATE TABLE stream_acl (
	stream_domain varchar(100) NOT NULL,
	stream_name varchar(100) NOT NULL,
	stream_version int4 NOT NULL,
	acl_id int8 NOT NULL,
	CONSTRAINT stream_acl_pkey PRIMARY KEY (stream_domain, stream_name, stream_version, acl_id),
	CONSTRAINT sa_a_fkey UNIQUE (acl_id)
);

ALTER TABLE stream_acl ADD CONSTRAINT sa_s_fkey FOREIGN KEY (stream_domain, stream_name, stream_version) REFERENCES stream(domain, name, version);
ALTER TABLE stream_acl ADD CONSTRAINT sa_u_fkey FOREIGN KEY (acl_id) REFERENCES user_permission(id);
