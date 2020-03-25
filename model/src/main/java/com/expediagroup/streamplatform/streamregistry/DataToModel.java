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
package com.expediagroup.streamplatform.streamregistry;

import java.util.ArrayList;
import java.util.List;

import com.expediagroup.streamplatform.streamregistry.data.Consumer;
import com.expediagroup.streamplatform.streamregistry.data.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.data.Domain;
import com.expediagroup.streamplatform.streamregistry.data.Infrastructure;
import com.expediagroup.streamplatform.streamregistry.data.Producer;
import com.expediagroup.streamplatform.streamregistry.data.ProducerBinding;
import com.expediagroup.streamplatform.streamregistry.data.Schema;
import com.expediagroup.streamplatform.streamregistry.data.Specification;
import com.expediagroup.streamplatform.streamregistry.data.Stream;
import com.expediagroup.streamplatform.streamregistry.data.StreamBinding;
import com.expediagroup.streamplatform.streamregistry.data.Tag;
import com.expediagroup.streamplatform.streamregistry.data.Zone;


public class DataToModel {

  public static com.expediagroup.streamplatform.streamregistry.model.Specification convertSpecification(Specification in) {
//    return new Specification(
//        in.getDescription(),
//        convertTags(in.getTags()),
//        in.getType(),
//        in.getConfiguration()
//    );
    throw new UnsupportedOperationException();
  }

  public static List<Tag> convertTags(List<com.expediagroup.streamplatform.streamregistry.data.Tag> in) {
    List<Tag> out = new ArrayList<>();
    for (com.expediagroup.streamplatform.streamregistry.data.Tag t : in) {
      out.add(new Tag(t.getId(), t.getName(), t.getValue()));
    }
    return out;
  }

  public static com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding convertConsumerBinding(ConsumerBinding consumerBinding) {
    throw new UnsupportedOperationException();
  }

  public static com.expediagroup.streamplatform.streamregistry.model.Consumer convertConsumer(Consumer consumer) {
    throw new UnsupportedOperationException();
  }

  public static com.expediagroup.streamplatform.streamregistry.model.Domain convertDomain(Domain consumer) {
    throw new UnsupportedOperationException();
  }

  public static com.expediagroup.streamplatform.streamregistry.model.Infrastructure convertInfrastructure(Infrastructure infrastructure) {
    throw new UnsupportedOperationException();
  }

  public static com.expediagroup.streamplatform.streamregistry.model.Producer convertProducer(Producer producer) {
    throw new UnsupportedOperationException();
  }

  public static com.expediagroup.streamplatform.streamregistry.model.ProducerBinding convertProducerBinding(ProducerBinding producerBinding) {
    throw new UnsupportedOperationException();
  }

  public static com.expediagroup.streamplatform.streamregistry.model.Schema convertSchema(Schema schema) {
    throw new UnsupportedOperationException();
  }

  public static com.expediagroup.streamplatform.streamregistry.model.Stream convertStream(Stream stream) {
    throw new UnsupportedOperationException();
  }

  public static com.expediagroup.streamplatform.streamregistry.model.StreamBinding convertStreamBinding(StreamBinding streamBinding) {
    throw new UnsupportedOperationException();
  }

  public static com.expediagroup.streamplatform.streamregistry.model.Zone convertZone(Zone zone) {
    throw new UnsupportedOperationException();
  }

}
