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
	CONSTRAINT uk_pqwiavnk8mg354eqn50tkxvhv UNIQUE (tags_id),
	CONSTRAINT fkhs0j1uycm0hfnha9d8y59o4e7 FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT fkjbmehtu8co8o1gwjqwrb2kkgp FOREIGN KEY (consumer_binding_consumer_name, consumer_binding_infrastructure_name, consumer_binding_infrastructure_zone, consumer_binding_stream_domain, consumer_binding_stream_name, consumer_binding_stream_version) REFERENCES consumer_binding(consumer_name, infrastructure_name, infrastructure_zone, stream_domain, stream_name, stream_version)
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
	CONSTRAINT uk_rk2fr5r7ocegsq46vklswkvu1 UNIQUE (tags_id),
	CONSTRAINT fkqbfa6brkql2cwnwk056cu2tnp FOREIGN KEY (consumer_name, consumer_stream_domain, consumer_stream_name, consumer_stream_version, consumer_zone) REFERENCES consumer(name, stream_domain, stream_name, stream_version, zone),
	CONSTRAINT fks9w0keceqchfvhdg9f9j9a70i FOREIGN KEY (tags_id) REFERENCES tag(id)
);

-- Drop table

-- DROP TABLE domain_tags;

CREATE TABLE domain_tags (
	domain_name varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT uk_boeunkl6y9lk0fqke7biwvlmq UNIQUE (tags_id),
	CONSTRAINT fkcw2kgmuhk3ypn8l98e4lnagvf FOREIGN KEY (domain_name) REFERENCES domain(name),
	CONSTRAINT fkkhpu6dsy48ryt2weaoikonpp0 FOREIGN KEY (tags_id) REFERENCES tag(id)
);

-- Drop table

-- DROP TABLE infrastructure_tags;

CREATE TABLE infrastructure_tags (
	infrastructure_name varchar(100) NOT NULL,
	infrastructure_zone varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT uk_c2d90c38vee279gijvuo3drh8 UNIQUE (tags_id),
	CONSTRAINT fknfcbulj4n4u6y7woa9e2ysmr3 FOREIGN KEY (infrastructure_name, infrastructure_zone) REFERENCES infrastructure(name, zone),
	CONSTRAINT fktagg7kpdv9a4e5gmu97q1vn60 FOREIGN KEY (tags_id) REFERENCES tag(id)
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
	CONSTRAINT uk_qnlwwsaea10ns6u3228e0w74c UNIQUE (tags_id),
	CONSTRAINT fkg7a3lawsso2ngxubfkxh08nf5 FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT fkpewya0dt9slqoano7e5ivv444 FOREIGN KEY (producer_binding_infrastructure_name, producer_binding_infrastructure_zone, producer_binding_producer_name, producer_binding_stream_domain, producer_binding_stream_name, producer_binding_stream_version) REFERENCES producer_binding(infrastructure_name, infrastructure_zone, producer_name, stream_domain, stream_name, stream_version)
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
	CONSTRAINT uk_md22ps9pqyvihngfbiq43bvqr UNIQUE (tags_id),
	CONSTRAINT fk8omxlah620qoe6k3fl5lifeew FOREIGN KEY (producer_name, producer_stream_domain, producer_stream_name, producer_stream_version, producer_zone) REFERENCES producer(name, stream_domain, stream_name, stream_version, zone),
	CONSTRAINT fkjdro73bjhxlv9pl2r52ru9yx5 FOREIGN KEY (tags_id) REFERENCES tag(id)
);

-- Drop table

-- DROP TABLE rword_schema_tags;

CREATE TABLE rword_schema_tags (
	rword_schema_skdomain varchar(100) NOT NULL,
	rword_schema_skname varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT uk_9m26e4v7cveajbtoyqbbj1r8a UNIQUE (tags_id),
	CONSTRAINT fkiqc1p5grut0w72ut48pmkp4mt FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT fkojeciao6pur9ej3p48m4ntpdq FOREIGN KEY (rword_schema_skdomain, rword_schema_skname) REFERENCES rword_schema(skdomain, skname)
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
	CONSTRAINT uk_qqrfnaftettq8937j5330nhpm UNIQUE (tags_id),
	CONSTRAINT fkdpowkdybwcgw3octmwdi6bats FOREIGN KEY (stream_binding_infrastructure_name, stream_binding_infrastructure_zone, stream_binding_stream_domain, stream_binding_stream_name, stream_binding_stream_version) REFERENCES stream_binding(infrastructure_name, infrastructure_zone, stream_domain, stream_name, stream_version),
	CONSTRAINT fkq0lxftkk9w9g6x61ct3s6qisx FOREIGN KEY (tags_id) REFERENCES tag(id)
);

-- Drop table

-- DROP TABLE stream_tags;

CREATE TABLE stream_tags (
	stream_domain varchar(100) NOT NULL,
	stream_name varchar(100) NOT NULL,
	stream_version int4 NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT uk_sqfmp0i65j477r5iuaoe7yo93 UNIQUE (tags_id),
	CONSTRAINT fk7cuiq41n5o8i3f0yteh0gh94e FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT fko9o8sa5unyttctbanh8w7u6au FOREIGN KEY (stream_domain, stream_name, stream_version) REFERENCES stream(domain, name, version)
);

-- Drop table

-- DROP TABLE zone_tags;

CREATE TABLE zone_tags (
	zone_name varchar(100) NOT NULL,
	tags_id int4 NOT NULL,
	CONSTRAINT uk_ddty77h480qj0pa7m17h4wbe1 UNIQUE (tags_id),
	CONSTRAINT fk4b7u4ool5snja8n54ki1sa8qf FOREIGN KEY (tags_id) REFERENCES tag(id),
	CONSTRAINT fkprjr91h68x1uocir6quvogcaq FOREIGN KEY (zone_name) REFERENCES zone(name)
);
