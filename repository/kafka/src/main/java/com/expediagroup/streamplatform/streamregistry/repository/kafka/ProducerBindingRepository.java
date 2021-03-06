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
package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ProducerBindingConverter;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;

@Component
public class ProducerBindingRepository
    extends DefaultRepository<ProducerBinding, ProducerBindingKey, Entity.ProducerBindingKey, DefaultSpecification>
    implements com.expediagroup.streamplatform.streamregistry.repository.ProducerBindingRepository {
  ProducerBindingRepository(EntityView view, EventSender sender, ProducerBindingConverter converter) {
    super(view, sender, converter, Entity.ProducerBindingKey.class);
  }

  @Override
  public List<ProducerBinding> findAll(ProducerBinding example) {
    // This method is deprecated. It only supports the existing use case
    // of searching for the ProducerBinding associated with a Producer
    return findAll().stream()
        .filter(pb -> pb.getKey().getProducerKey().equals(example.getKey().getProducerKey()))
        .collect(toList());
  }

}
