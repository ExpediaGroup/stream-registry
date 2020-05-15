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
package com.expediagroup.streamplatform.streamregistry.state.kafka;



import lombok.extern.slf4j.Slf4j;




@Slf4j
public class KafkaEventReceiverTest {

//  @Rule
//  public KafkaContainer kafka = new KafkaContainer();
//
//  @Test
//  public void test() throws Exception {
//    Map<String, Object> senderConfig = Map.of(
//        BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
//        SCHEMA_REGISTRY_URL_CONFIG, "mock://schemas",
//        TOPIC_CONFIG, "topic"
//    );
//
//    KafkaEventSender.Factory senderFactory = new KafkaEventSender.Factory(senderConfig, null);
//    EventSender sender = senderFactory.create();
//
//    Entity.DomainKey domainKey = new Entity.DomainKey("domain");
//    DefaultSpecification specification = new DefaultSpecification("d", List.of(), "type", new ObjectMapper().createObjectNode());
//
////    Flux.interval(Duration.ofSeconds(1))
////        .doOnNext(x -> sender.send(Event.of(domainKey, specification)))
////        .subscribe();
//
//    Thread.sleep(Duration.ofSeconds(5).toMillis());
//
//    Map<String, Object> receiverConfig = Map.of(
//        BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
//        GROUP_ID_CONFIG, "myApp",
//        SCHEMA_REGISTRY_URL_CONFIG, "mock://schemas",
//        TOPIC_CONFIG, "topic"
//    );
//    EventReceiver.Factory receiverFactory = new KafkaEventReceiver.Factory(receiverConfig, null);
//    EntityView entityView = EntityView.create(receiverFactory);
//
//    entityView.load(this::onEvent).join();
//
////    Flux.interval(Duration.ofSeconds(5)).doOnNext(x -> {
////      log.info("Entity: {}", entityView.get(new Entity.DomainKey("domain")));
////    }).then().block();
//
//    Thread.sleep(Duration.ofHours(1).toMillis());
//  }
//
//  private void onEvent(Entity oldEntity, Event event) {
//    log.info("Event: {}", event);
//  }
//
//
//  AvroKey key() {
//    return new AvroKey(
//        new AvroSpecificationKey(
//            new AvroDomainKey(("name"))
//        )
//    );
//  }
//
//  AtomicLong x = new AtomicLong();
//
//  AvroValue value() {
//    return new AvroValue(
//        new AvroSpecification(
//            "description" + x.incrementAndGet(),
//            List.of(new AvroTag("name", "value")),
//            "type",
//            new AvroObject(Map.of())
//        )
//    );
//  }

}
