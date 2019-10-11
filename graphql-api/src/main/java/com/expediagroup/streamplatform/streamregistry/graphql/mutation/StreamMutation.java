/**
 * Copyright (C) 2018-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.graphql.mutation;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

public class StreamMutation {

  private Services services;

  public StreamMutation(Services services) {
    this.services = services;
  }

  public Stream insert(StreamKeyInput key, SpecificationInput specification, SchemaKeyInput schema) {
    return services.getStreamService().create(asStream(key, specification, schema)).get();
  }

  public Stream update(StreamKeyInput key, SpecificationInput specification) {
    return services.getStreamService().update(asStream(key, specification, null)).get();
  }

  public Stream upsert(StreamKeyInput key, SpecificationInput specification, SchemaKeyInput schema) {
    return services.getStreamService().upsert(asStream(key, specification, schema)).get();
  }

  public Boolean delete(StreamKeyInput key) {
    throw new UnsupportedOperationException("deleteStream");
  }

  public Stream updateStatus(StreamKeyInput key, StatusInput status) {
    Stream stream = services.getStreamService().read(key.asStreamKey()).get();
    stream.setStatus(status.asStatus());
    return services.getStreamService().update(stream).get();
  }

  private Stream asStream(StreamKeyInput key, SpecificationInput specification, SchemaKeyInput schema) {
    Stream stream = new Stream();
    stream.setKey(key.asStreamKey());
    stream.setSpecification(specification.asSpecification());
    stream.setSchemaKey(schema.asSchemaKey());
    maintainState(stream, services.getStreamService().read(stream.getKey()));
    return stream;
  }
}
