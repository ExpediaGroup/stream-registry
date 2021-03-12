/**
 * Copyright (C) 2018-2021 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.services;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerBindingRepository;
import com.expediagroup.streamplatform.streamregistry.repository.ProducerBindingRepository;

@Component
@RequiredArgsConstructor
public class UtilService {
  private final ProducerBindingRepository producerBindingRepository;
  private final ConsumerBindingRepository consumerBindingRepository;

  // Used to delete producer/consumer bindings when deleting stream or stream binding
  @PreAuthorize("hasPermission('DELETE')")
  public void findAllAndDelete(StreamBindingKey key) {
    val producerKey = new ProducerKey(
      key.getStreamDomain(),
      key.getStreamName(),
      key.getStreamVersion(),
      key.getInfrastructureZone(),
      key.getInfrastructureName()
    );
    producerBindingRepository.findAllAndDelete(producerKey);
    val consumerKey = new ConsumerKey(
      key.getStreamDomain(),
      key.getStreamName(),
      key.getStreamVersion(),
      key.getInfrastructureZone(),
      key.getInfrastructureName()
    );
    consumerBindingRepository.findAllAndDelete(consumerKey);
  }
}
