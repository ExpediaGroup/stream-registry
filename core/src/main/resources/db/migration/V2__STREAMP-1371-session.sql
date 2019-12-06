
-- Drop table

-- DROP TABLE "session";

CREATE TABLE "session" (
	id varchar(255) NOT NULL,
	expires_at int8 NOT NULL,
	secret varchar(255) NOT NULL,
	CONSTRAINT session_pkey PRIMARY KEY (id)
);

-- Drop table

-- DROP TABLE session_consumer_bindings;

CREATE TABLE session_consumer_bindings (
	session_id varchar(255) NOT NULL,
	consumer_bindings_consumer_name varchar(100) NOT NULL,
	consumer_bindings_infrastructure_name varchar(100) NOT NULL,
	consumer_bindings_infrastructure_zone varchar(100) NOT NULL,
	consumer_bindings_stream_domain varchar(100) NOT NULL,
	consumer_bindings_stream_name varchar(100) NOT NULL,
	consumer_bindings_stream_version int4 NOT NULL,
	CONSTRAINT session_consumer_bindings_pkey PRIMARY KEY (session_id, consumer_bindings_consumer_name, consumer_bindings_infrastructure_name, consumer_bindings_infrastructure_zone, consumer_bindings_stream_domain, consumer_bindings_stream_name, consumer_bindings_stream_version)
);

-- Drop table

-- DROP TABLE session_producer_bindings;

CREATE TABLE session_producer_bindings (
	session_id varchar(255) NOT NULL,
	producer_bindings_infrastructure_name varchar(100) NOT NULL,
	producer_bindings_infrastructure_zone varchar(100) NOT NULL,
	producer_bindings_producer_name varchar(100) NOT NULL,
	producer_bindings_stream_domain varchar(100) NOT NULL,
	producer_bindings_stream_name varchar(100) NOT NULL,
	producer_bindings_stream_version int4 NOT NULL,
	CONSTRAINT session_producer_bindings_pkey PRIMARY KEY (session_id, producer_bindings_infrastructure_name, producer_bindings_infrastructure_zone, producer_bindings_producer_name, producer_bindings_stream_domain, producer_bindings_stream_name, producer_bindings_stream_version)
);

ALTER TABLE session_consumer_bindings ADD CONSTRAINT scb_s_fk FOREIGN KEY (session_id) REFERENCES session(id);
ALTER TABLE session_consumer_bindings ADD CONSTRAINT scb_cb_fk FOREIGN KEY (consumer_bindings_consumer_name, consumer_bindings_infrastructure_name, consumer_bindings_infrastructure_zone, consumer_bindings_stream_domain, consumer_bindings_stream_name, consumer_bindings_stream_version) REFERENCES consumer_binding(consumer_name, infrastructure_name, infrastructure_zone, stream_domain, stream_name, stream_version);

ALTER TABLE session_producer_bindings ADD CONSTRAINT spb_s_fk FOREIGN KEY (session_id) REFERENCES session(id);
ALTER TABLE session_producer_bindings ADD CONSTRAINT spb_pb_fk FOREIGN KEY (producer_bindings_infrastructure_name, producer_bindings_infrastructure_zone, producer_bindings_producer_name, producer_bindings_stream_domain, producer_bindings_stream_name, producer_bindings_stream_version) REFERENCES producer_binding(infrastructure_name, infrastructure_zone, producer_name, stream_domain, stream_name, stream_version);
