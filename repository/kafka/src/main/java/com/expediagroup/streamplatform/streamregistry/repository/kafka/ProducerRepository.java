/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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

import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Producer;
import com.expediagroup.streamplatform.streamregistry.model.keys.ProducerKey;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.Converter.ProducerConverter;
import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class ProducerRepository
    extends DefaultRepository<Producer, ProducerKey, Entity.ProducerKey, DefaultSpecification>
    implements com.expediagroup.streamplatform.streamregistry.repository.ProducerRepository {
  ProducerRepository(EntityView view, EventSender sender, ProducerConverter converter) {
    super(view, sender, converter, Entity.ProducerKey.class);
  }

  @Override
  public List<Producer> findAll(Producer example) {
    return findAll().stream()
            .filter(pb -> pb.getKey().getStreamKey().equals(example.getKey().getStreamKey()))
            .collect(toList());
  }

  public void findAllAndDelete(StreamKey key) {
    findAll().stream()
      .filter(p -> p.getKey().getStreamKey().equals(key));
  }
}
