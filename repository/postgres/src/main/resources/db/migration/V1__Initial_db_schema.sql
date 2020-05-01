CREATE SEQUENCE hibernate_sequence START 1 INCREMENT 1;

-- Drop table

-- DROP TABLE consumer;

CREATE TABLE consumer (
	name varchar(100) NOT NULL,
	stream_domain varchar(100) NOT NULL,
	stream_name varchar(100) NOT NULL,
	stream_version int4 NOT NULL,
	"zone" varchar(100) NOT NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT consumer_pkey PRIMARY KEY (name, stream_domain, stream_name, stream_version, zone)
);

-- Drop table

-- DROP TABLE consumer_binding;

CREATE TABLE consumer_binding (
	consumer_name varchar(100) NOT NULL,
	infrastructure_name varchar(100) NOT NULL,
	infrastructure_zone varchar(100) NOT NULL,
	stream_domain varchar(100) NOT NULL,
	stream_name varchar(100) NOT NULL,
	stream_version int4 NOT NULL,
	binding_id varchar(255) NULL,
	consumer_id varchar(255) NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT consumer_binding_pkey PRIMARY KEY (consumer_name, infrastructure_name, infrastructure_zone, stream_domain, stream_name, stream_version)
);

-- Drop table

-- DROP TABLE "domain";

CREATE TABLE "domain" (
	name varchar(100) NOT NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT domain_pkey PRIMARY KEY (name)
);

-- Drop table

-- DROP TABLE infrastructure;

CREATE TABLE infrastructure (
	name varchar(100) NOT NULL,
	"zone" varchar(100) NOT NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT infrastructure_pkey PRIMARY KEY (name, zone)
);

-- Drop table

-- DROP TABLE producer;

CREATE TABLE producer (
	name varchar(100) NOT NULL,
	stream_domain varchar(100) NOT NULL,
	stream_name varchar(100) NOT NULL,
	stream_version int4 NOT NULL,
	"zone" varchar(100) NOT NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT producer_pkey PRIMARY KEY (name, stream_domain, stream_name, stream_version, zone)
);

-- Drop table

-- DROP TABLE producer_binding;

CREATE TABLE producer_binding (
	infrastructure_name varchar(100) NOT NULL,
	infrastructure_zone varchar(100) NOT NULL,
	producer_name varchar(100) NOT NULL,
	stream_domain varchar(100) NOT NULL,
	stream_name varchar(100) NOT NULL,
	stream_version int4 NOT NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT producer_binding_pkey PRIMARY KEY (infrastructure_name, infrastructure_zone, producer_name, stream_domain, stream_name, stream_version)
);

-- Drop table

-- DROP TABLE rword_schema;

CREATE TABLE rword_schema (
	skdomain varchar(100) NOT NULL,
	skname varchar(100) NOT NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT rword_schema_pkey PRIMARY KEY (skdomain, skname)
);

-- Drop table

-- DROP TABLE stream;

CREATE TABLE stream (
	"domain" varchar(100) NOT NULL,
	name varchar(100) NOT NULL,
	"version" int4 NOT NULL,
	skdomain varchar(100) NULL,
	skname varchar(100) NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT stream_pkey PRIMARY KEY (domain, name, version)
);

-- Drop table

-- DROP TABLE stream_binding;

CREATE TABLE stream_binding (
	infrastructure_name varchar(100) NOT NULL,
	infrastructure_zone varchar(100) NOT NULL,
	stream_domain varchar(100) NOT NULL,
	stream_name varchar(100) NOT NULL,
	stream_version int4 NOT NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT stream_binding_pkey PRIMARY KEY (infrastructure_name, infrastructure_zone, stream_domain, stream_name, stream_version)
);

-- Drop table

-- DROP TABLE tag;

CREATE TABLE tag (
	id int4 NOT NULL,
	name varchar(255) NULL,
	value varchar(255) NULL,
	CONSTRAINT tag_pkey PRIMARY KEY (id)
);

-- Drop table

-- DROP TABLE "zone";

CREATE TABLE "zone" (
	name varchar(100) NOT NULL,
	config_json text NULL,
	description varchar(255) NULL,
	rword_type varchar(255) NULL,
	status_json text NULL,
	CONSTRAINT zone_pkey PRIMARY KEY (name)
);

-- Drop table

-- DROP TABLE consumer_binding_tags;

CREATE TABLE consumer_binding_tags (
	consumer_binding_consumer_name varchar(100) NOT NULL,
	consumer_binding_infrastructure_name varchar(100) NOT NULL,
	consumer_binding_infrastructure_zone varchar(100) NOT NULL,
	consumer_binding_stream_domain varchar(100) NOT NULL,
	consumer_binding_stream_name varchar(100) NOT NULL,
	consumer_binding_stream_version int4 NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT cbd_ti_ukey UNIQUE (tags_id),
	CONSTRAINT cbd_t_fk FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT cbd_cb_fk FOREIGN KEY (consumer_binding_consumer_name, consumer_binding_infrastructure_name, consumer_binding_infrastructure_zone, consumer_binding_stream_domain, consumer_binding_stream_name, consumer_binding_stream_version) REFERENCES consumer_binding(consumer_name, infrastructure_name, infrastructure_zone, stream_domain, stream_name, stream_version)
);

-- Drop table

-- DROP TABLE consumer_tags;

CREATE TABLE consumer_tags (
	consumer_name varchar(100) NOT NULL,
	consumer_stream_domain varchar(100) NOT NULL,
	consumer_stream_name varchar(100) NOT NULL,
	consumer_stream_version int4 NOT NULL,
	consumer_zone varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT ct_ti_ukey UNIQUE (tags_id),
	CONSTRAINT ct_c_fkey FOREIGN KEY (consumer_name, consumer_stream_domain, consumer_stream_name, consumer_stream_version, consumer_zone) REFERENCES consumer(name, stream_domain, stream_name, stream_version, zone),
	CONSTRAINT ct_t_fkey FOREIGN KEY (tags_id) REFERENCES tag(id)
);

-- Drop table

-- DROP TABLE domain_tags;

CREATE TABLE domain_tags (
	domain_name varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT dt_ti_ukey UNIQUE (tags_id),
	CONSTRAINT dt_d_fkey FOREIGN KEY (domain_name) REFERENCES domain(name),
	CONSTRAINT dt_t_fkey FOREIGN KEY (tags_id) REFERENCES tag(id)
);

-- Drop table

-- DROP TABLE infrastructure_tags;

CREATE TABLE infrastructure_tags (
	infrastructure_name varchar(100) NOT NULL,
	infrastructure_zone varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT it_ti_ukey UNIQUE (tags_id),
	CONSTRAINT it_i_fkey FOREIGN KEY (infrastructure_name, infrastructure_zone) REFERENCES infrastructure(name, zone),
	CONSTRAINT it_t_fkey FOREIGN KEY (tags_id) REFERENCES tag(id)
);

-- Drop table

-- DROP TABLE producer_binding_tags;

CREATE TABLE producer_binding_tags (
	producer_binding_infrastructure_name varchar(100) NOT NULL,
	producer_binding_infrastructure_zone varchar(100) NOT NULL,
	producer_binding_producer_name varchar(100) NOT NULL,
	producer_binding_stream_domain varchar(100) NOT NULL,
	producer_binding_stream_name varchar(100) NOT NULL,
	producer_binding_stream_version int4 NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT pbt_ti_ukey UNIQUE (tags_id),
	CONSTRAINT pbt_t_fkey FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT pbt_pb_fkey FOREIGN KEY (producer_binding_infrastructure_name, producer_binding_infrastructure_zone, producer_binding_producer_name, producer_binding_stream_domain, producer_binding_stream_name, producer_binding_stream_version) REFERENCES producer_binding(infrastructure_name, infrastructure_zone, producer_name, stream_domain, stream_name, stream_version)
);

-- Drop table

-- DROP TABLE producer_tags;

CREATE TABLE producer_tags (
	producer_name varchar(100) NOT NULL,
	producer_stream_domain varchar(100) NOT NULL,
	producer_stream_name varchar(100) NOT NULL,
	producer_stream_version int4 NOT NULL,
	producer_zone varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT pt_ti_ukey UNIQUE (tags_id),
	CONSTRAINT pt_p_fkey FOREIGN KEY (producer_name, producer_stream_domain, producer_stream_name, producer_stream_version, producer_zone) REFERENCES producer(name, stream_domain, stream_name, stream_version, zone),
	CONSTRAINT pt_t_fkey FOREIGN KEY (tags_id) REFERENCES tag(id)
);

-- Drop table

-- DROP TABLE rword_schema_tags;

CREATE TABLE rword_schema_tags (
	rword_schema_skdomain varchar(100) NOT NULL,
	rword_schema_skname varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT rst_ti_ukey UNIQUE (tags_id),
	CONSTRAINT rst_t_fkey FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT rst_rs_fkey FOREIGN KEY (rword_schema_skdomain, rword_schema_skname) REFERENCES rword_schema(skdomain, skname)
);

-- Drop table

-- DROP TABLE stream_binding_tags;

CREATE TABLE stream_binding_tags (
	stream_binding_infrastructure_name varchar(100) NOT NULL,
	stream_binding_infrastructure_zone varchar(100) NOT NULL,
	stream_binding_stream_domain varchar(100) NOT NULL,
	stream_binding_stream_name varchar(100) NOT NULL,
	stream_binding_stream_version int4 NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT sbd_ti_ukey UNIQUE (tags_id),
	CONSTRAINT sbd_sb_fkey FOREIGN KEY (stream_binding_infrastructure_name, stream_binding_infrastructure_zone, stream_binding_stream_domain, stream_binding_stream_name, stream_binding_stream_version) REFERENCES stream_binding(infrastructure_name, infrastructure_zone, stream_domain, stream_name, stream_version),
	CONSTRAINT sbd_t_fkey FOREIGN KEY (tags_id) REFERENCES tag(id)
);

-- Drop table

-- DROP TABLE stream_tags;

CREATE TABLE stream_tags (
	stream_domain varchar(100) NOT NULL,
	stream_name varchar(100) NOT NULL,
	stream_version int4 NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT st_ti_ukey UNIQUE (tags_id),
	CONSTRAINT st_t_fkey FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT st_s_fkey FOREIGN KEY (stream_domain, stream_name, stream_version) REFERENCES stream(domain, name, version)
);

-- Drop table

-- DROP TABLE zone_tags;

CREATE TABLE zone_tags (
	zone_name varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT zt_ti_ukey UNIQUE (tags_id),
	CONSTRAINT zt_t_fkey FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT zt_z_fkey FOREIGN KEY (zone_name) REFERENCES zone(name)
);
