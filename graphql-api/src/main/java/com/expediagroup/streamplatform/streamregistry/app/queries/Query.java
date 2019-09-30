package com.expediagroup.streamplatform.streamregistry.app.queries;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
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
import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.model.Zone;

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