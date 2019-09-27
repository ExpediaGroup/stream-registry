package com.expediagroup.streamplatform.streamregistry.app.queries;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.expediagroup.streamplatform.streamregistry.app.Consumer;
import com.expediagroup.streamplatform.streamregistry.app.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.app.Domain;
import com.expediagroup.streamplatform.streamregistry.app.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.app.Producer;
import com.expediagroup.streamplatform.streamregistry.app.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.app.Schema;
import com.expediagroup.streamplatform.streamregistry.app.Stream;
import com.expediagroup.streamplatform.streamregistry.app.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.app.Zone;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ConsumerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ConsumerKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.InfrastructureKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ProducerBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ProducerKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.TagInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.ZoneKeyInput;

public interface Query extends GraphQLQueryResolver {

  Domain bugfixq(
      TagQuery v
  );

  Domain bugfixi(
      TagInput v
  );

  Domain getDomain(
      DomainKeyInput key
  );

  Iterable<Domain> getDomains(
      DomainKeyQuery key, SpecificationQuery specification
  );

  Schema getSchema(
      SchemaKeyInput key
  );

  Iterable<Schema> getSchemas(
      SchemaKeyQuery key, SpecificationQuery specification
  );

  Stream getStream(
      StreamKeyInput key
  );

  Iterable<Stream> getStreams(
      StreamKeyQuery key, SpecificationQuery specification, SchemaKeyQuery schema
  );

  Zone getZone(
      ZoneKeyInput key
  );

  Iterable<Zone> getZones(
      ZoneKeyQuery key, SpecificationQuery specification
  );

  Infrastructure getInfrastructure(
      InfrastructureKeyInput key
  );

  Iterable<Infrastructure> getInfrastructures(
      InfrastructureKeyQuery key, SpecificationQuery specification
  );

  Producer getProducer(
      ProducerKeyInput key
  );

  Iterable<Producer> getProducers(
      ProducerKeyQuery key, SpecificationQuery specification
  );

  Consumer getConsumer(
      ConsumerKeyInput key
  );

  Iterable<Consumer> getConsumers(
      ConsumerKeyQuery key, SpecificationQuery specification
  );

  StreamBinding getStreamBinding(
      StreamBindingKeyInput key
  );

  Iterable<StreamBinding> getStreamBindings(
      StreamBindingKeyQuery key, SpecificationQuery specification
  );

  ProducerBinding getProducerBinding(
      ProducerBindingKeyInput key
  );

  Iterable<ProducerBinding> getProducerBindings(
      ProducerBindingKeyQuery key, SpecificationQuery specification
  );

  ConsumerBinding getConsumerBinding(
      ConsumerBindingKeyInput key
  );

  Iterable<ConsumerBinding> getConsumerBindings(
      ConsumerBindingKeyQuery key, SpecificationQuery specification
  );
}