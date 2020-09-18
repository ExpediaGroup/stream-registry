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
package com.expediagroup.streamplatform.streamregistry.cli.command.delete;

import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.specificationDeletion;
import static com.expediagroup.streamplatform.streamregistry.state.model.event.Event.statusDeletion;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Option;

import com.expediagroup.streamplatform.streamregistry.state.kafka.KafkaEventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.Key;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

@RequiredArgsConstructor
class EntityDeleter implements Closeable {
  private final KafkaEventSender sender;
  private final BlockingQueue<CompletableFuture<?>> queue = new LinkedBlockingDeque<>();

  <S extends Specification> void delete(Key<S> key) {
    queue.add(sender.send(statusDeletion(key, "agentStatus")));
    queue.add(sender.send(specificationDeletion(key)));
  }

  @Override
  public void close() {
    waitForFutures();
    sender.close();
  }

  private void waitForFutures() {
    List<CompletableFuture<?>> futures = new ArrayList<>();
    queue.drainTo(futures);
    futures.forEach(CompletableFuture::join);
  }

  static class Factory {
    @Option(names = "--bootstrapServers", required = true) String bootstrapServers;
    @Option(names = "--topic", required = true, defaultValue = "_streamregistry") String topic;
    @Option(names = "--schemaRegistryUrl", required = true) String schemaRegistryUrl;

    EntityDeleter create() {
      KafkaEventSender.Config config = KafkaEventSender.Config.builder()
          .bootstrapServers(bootstrapServers)
          .topic(topic)
          .schemaRegistryUrl(schemaRegistryUrl)
          .build();
      KafkaEventSender sender = new KafkaEventSender(config);
      return new EntityDeleter(sender);
    }
  }
}
