/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.graphql.mutation.impl;

import static com.expediagroup.streamplatform.streamregistry.graphql.StateHelper.maintainState;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.services.StreamService;
import com.expediagroup.streamplatform.streamregistry.core.views.StreamView;
import com.expediagroup.streamplatform.streamregistry.graphql.StateHelper;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SchemaKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.StreamKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.mutation.StreamMutation;
import com.expediagroup.streamplatform.streamregistry.model.Stream;

@Component
@RequiredArgsConstructor
public class StreamMutationImpl implements StreamMutation {

  @Value("${entityView.exist.check.enabled:true}")
  private boolean checkExistEnabled;

  @Value("${stream-registry.entity.status.enabled:true}")
  private boolean entityStatusEnabled;

  private final StreamService streamService;
  private final StreamView streamView;

  @Override
  public Stream insert(StreamKeyInput key, SpecificationInput specification, SchemaKeyInput schema) {
    return streamService.create(asStream(key, specification, Optional.of(schema))).get();
  }

  @Override
  public Stream update(StreamKeyInput key, SpecificationInput specification) {
    return streamService.update(asStream(key, specification, Optional.empty())).get();
  }

  @Override
  public Stream upsert(StreamKeyInput key, SpecificationInput specification, SchemaKeyInput schema) {
    Stream stream = asStream(key, specification, Optional.ofNullable(schema));
    if (!streamView.get(stream.getKey()).isPresent()) {
      return streamService.create(stream).get();
    } else {
      return streamService.update(stream).get();
    }
  }

  @Override
  public Boolean delete(StreamKeyInput key) {
    Optional<Stream> stream = streamView.get(key.asStreamKey());
    stream.ifPresentOrElse(streamService::delete, () -> {
      if (!checkExistEnabled) {
        streamService.delete(new Stream(key.asStreamKey(), StateHelper.schemaKey(), StateHelper.specification(), StateHelper.status()));
      }
    });
    return true;
  }

  @Override
  public Stream updateStatus(StreamKeyInput key, StatusInput status) {
    Stream stream = streamView.get(key.asStreamKey()).get();

    if (entityStatusEnabled) {
      return streamService.updateStatus(stream, status.asStatus()).get();
    } else {
      return stream;
    }
  }

  private Stream asStream(StreamKeyInput key, SpecificationInput specification, Optional<SchemaKeyInput> schema) {
    Stream stream = new Stream();
    stream.setKey(key.asStreamKey());
    stream.setSpecification(specification.asSpecification());
    if (schema.isPresent()) {
      stream.setSchemaKey(schema.get().asSchemaKey());
    }
    maintainState(stream, streamView.get(stream.getKey()));
    return stream;
  }
}
