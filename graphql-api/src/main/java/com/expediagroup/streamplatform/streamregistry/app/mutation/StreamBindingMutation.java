/**
 * Copyright (C) 2016-2019 Expedia Inc.
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

package com.expediagroup.streamplatform.streamregistry.app.mutation;

import static com.expediagroup.streamplatform.streamregistry.app.StateHelper.maintainState;

import com.expediagroup.streamplatform.streamregistry.app.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StatusInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.StreamBindingKeyInput;
import com.expediagroup.streamplatform.streamregistry.core.services.Services;
import com.expediagroup.streamplatform.streamregistry.model.StreamBinding;

public class StreamBindingMutation {

  private Services services;

  public StreamBindingMutation(Services services) {
    this.services = services;
  }

  public StreamBinding insert(StreamBindingKeyInput key, SpecificationInput specification) {
    return services.getStreamBindingService().create(asStreamBinding(key, specification)).get();
  }

  public StreamBinding update(StreamBindingKeyInput key, SpecificationInput specification) {
    return services.getStreamBindingService().update(asStreamBinding(key, specification)).get();
  }

  public StreamBinding upsert(StreamBindingKeyInput key, SpecificationInput specification) {
    return services.getStreamBindingService().upsert(asStreamBinding(key, specification)).get();
  }

  public Boolean delete(StreamBindingKeyInput key) {
    throw new UnsupportedOperationException("delete");
  }

  private StreamBinding asStreamBinding(StreamBindingKeyInput key, SpecificationInput specification) {
    StreamBinding streamBinding = new StreamBinding();
    streamBinding.setKey(key.asStreamBindingKey());
    streamBinding.setSpecification(specification.asSpecification());
    maintainState(streamBinding, services.getStreamBindingService().read(streamBinding.getKey()));
    return streamBinding;
  }

  public StreamBinding updateStatus(StreamBindingKeyInput key, StatusInput status) {
    StreamBinding streamBinding = services.getStreamBindingService().read(key.asStreamBindingKey()).get();
    streamBinding.setStatus(status.asStatus());
    return services.getStreamBindingService().update(streamBinding).get();
  }
}

