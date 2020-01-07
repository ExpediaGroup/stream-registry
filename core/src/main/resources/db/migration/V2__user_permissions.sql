
CREATE TABLE user_permission (
	id int8 NOT NULL,
	permission varchar(255) NULL,
	principal varchar(255) NULL,
	CONSTRAINT user_permission_pkey PRIMARY KEY (id)
);

CREATE TABLE stream_access_control_list (
	stream_domain varchar(100) NOT NULL,
	stream_name varchar(100) NOT NULL,
	stream_version int4 NOT NULL,
	access_control_list_id int8 NOT NULL,
	CONSTRAINT stream_access_control_list_pkey PRIMARY KEY (stream_domain, stream_name, stream_version, access_control_list_id),
	CONSTRAINT sa_a_fkey UNIQUE (access_control_list_id)
);

ALTER TABLE stream_access_control_list ADD CONSTRAINT sa_s_fkey FOREIGN KEY (stream_domain, stream_name, stream_version) REFERENCES stream(domain, name, version);
ALTER TABLE stream_access_control_list ADD CONSTRAINT sa_u_fkey FOREIGN KEY (access_control_list_id) REFERENCES user_permission(id);
