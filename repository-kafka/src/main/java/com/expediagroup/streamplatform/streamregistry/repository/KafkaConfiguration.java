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
package com.expediagroup.streamplatform.streamregistry.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.model.Schema;
import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroDomainConversion;
import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroSchemaConversion;
import com.expediagroup.streamplatform.streamregistry.repository.avro.AvroStreamConversion;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.KafkaRepository;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.StoreProducer;
import com.expediagroup.streamplatform.streamregistry.repository.kafka.StoreView;

@Configuration
public class KafkaConfiguration {
  @Autowired
  StoreProducer producer;
  @Autowired
  StoreView view;

  @Bean
  Repository<Domain, Domain.Key> domainRepository(AvroDomainConversion conversion) {
    return new KafkaRepository<>(producer, view, conversion);
  }

  @Bean
  Repository<Schema, Schema.Key> schemaRepository(AvroSchemaConversion conversion) {
    return new KafkaRepository<>(producer, view, conversion);
  }

  @Bean
  Repository<Stream, Stream.Key> streamRepository(AvroStreamConversion conversion) {
    return new KafkaRepository<>(producer, view, conversion);
  }
}
