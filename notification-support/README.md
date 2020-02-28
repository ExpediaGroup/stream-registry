## stream-registry's notification-support module
### What is it?
It set of components which provides an easy way to **listen** events carried out in the `stream-registry` core, 
specifically _CREATE_, _UPDATE_ and _DELETE_ model entities (like streams, schemas or domains) in order to communicate
them to available handlers.

### What does it include?
#### 1. A set of listeners
In package `com.expediagroup.streamplatform.streamregistry.core.listeners` which includes listeners for `Schema` and 
`Stream` entities.

#### 2. A _Kafka_ handler for a particular model entity
It is in package `com.expediagroup.streamplatform.streamregistry.core.handlers` which includes a _Kafka_ event handler 
for `Schema` entities.

#### 3. Automatic configuration for _Kafka_
It is an optionally loadable functionality which makes possible set a handler to push Schema change events to a _Kafka_ 
topic. It will be explained on following section.

### _Kafka_ automatic configuration
Since this is an optionally loadable functionality, it will be ignored if properties aren't set.

#### `notification.events.kafka.enabled = [true | false] (default: not set)`
This is the butter and bread of the _Kafka_ interaction, once set it will try to build the Kafka related components 
verifying the following properties:

##### `notification.events.kafka.bootstrap-servers (default: not set)`
A comma separated list of _Kafka_ brokers.

##### `notification.events.kafka.schema.registry.url (default: not set)`
A schema registry URL

##### `notification.events.kafka.topic (default: not set)`
The topic name where the messages are going to be pushed.

##### `notification.events.kafka.topic.setup = [true | false] (default: false)`
If enabled it will validate the existence of `notification.events.kafka.topic` and will create it if topic doesn't 
exist.
#### Bootstrapping
Once configuration set, the application builds the components following actions (order may vary):

1. Build a `ProducerFactory` and a `KafkaTemplate` (the _Kafka_ producer)
2. Having the `KafkaTemplate`, a `SchemaEventHandlerForKafka` is built and it is injected to the list of handlers 
contained in `SchemaNotificationEventListener` (which notify schema changes in an observer pattern fashion).
3. Build a `KafkaSetupHandler` which performs a setup process when bean is loaded by checking that notification topic 
actually exists in the cluster and/or create it (if `notification.events.kafka.topic.setup` is set to `true`).

#### _Kafka_ producer customization
By default an avro protocol has been set (`src/main/resources/avro/stream-registry-notification.avdl`) which describes 
an schema that can be used for most of the `stream-registry` core entities, however for the `Schema` type (and in the 
future for more core types) an external dependency configuration can be set by setting some properties.
Let say we have a maven dependency containing static methods to transform `stream-registry`'s `Schema` entities to our 
own avro protocol, then we can set something like:

```
    notification.events.kafka.custom.schema.custom-enabled=true
    notification.events.kafka.custom.schema.key-parser-class=com.custom.MyClassWithAnStaticKeyParser
    notification.events.kafka.custom.schema.key-parser-method=streamRegistrySchemaToSpecificRecordKey
    notification.events.kafka.custom.schema.value-parser-class=com.custom.MyClassWithAnStaticValueParser
    notification.events.kafka.custom.schema.value-parser-method=streamRegistrySchemaToSpecificRecordValue
```

And the statics methods will be loaded by reflection and used in `SchemaEventHandlerForKafka`. (You can take a look at
`CustomSchemaMethodsLoadingTest` where this feature is tested).