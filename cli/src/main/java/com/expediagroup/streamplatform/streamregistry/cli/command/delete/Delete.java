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

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toSet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import com.expediagroup.streamplatform.streamregistry.cli.action.Action;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.Key;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.SchemaKey;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.StreamKey;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;

@Command(name = "delete", subcommands = {
    Delete.Domain.class,
    Delete.Schema.class,
    Delete.Stream.class,
    Delete.Zone.class,
    Delete.Infrastructure.class,
    Delete.Producer.class,
    Delete.Consumer.class,
    Delete.StreamBinding.class,
    Delete.ProducerBinding.class,
    Delete.ConsumerBinding.class
})
public class Delete {
  static abstract class Base implements Action {
    private final Formatter formatter = new Formatter();
    @Mixin EntityClient.Factory clientFactory;
    @Mixin EntityDeleter.Factory deleterFactory;
    @Option(names = "--dryRun") boolean dryRun;

    @SneakyThrows
    @Override
    public void run(PrintStream out, PrintStream err) {
      var client = clientFactory.create();
      try (var deleter = deleterFactory.create()) {
        run(out, err, client, deleter);
      }
    }

    void run(PrintStream out, PrintStream err, EntityClient client, EntityDeleter deleter) {
      List<Key<? extends Specification>> entities = findEntities(client);
      if (entities.isEmpty()) {
        err.println("No entities found!");
        return;
      }
      java.util.function.Consumer<Key<? extends Specification>> report = k -> out.println(formatter.format(k));
      if (dryRun) {
        entities.forEach(report);
      } else {
        entities.forEach(report.andThen(deleter::delete));
      }
    }

    abstract List<Key<? extends Specification>> findEntities(EntityClient client);
  }

  @Command(name = "domain")
  public static class Domain extends Base {
    @Option(names = "--domain", required = true) private String domain;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      return unmodifiableList(client.getDomainKeys(domain));
    }
  }

  @Command(name = "schema")
  static class Schema extends Base {
    @Option(names = "--domain", required = true) private String domain;
    @Option(names = "--schema", required = true) private String schema;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      return unmodifiableList(client.getSchemaKeys(domain, schema));
    }
  }

  @Command(name = "stream")
  static class Stream extends Base {
    @Option(names = "--domain", required = true) private String domain;
    @Option(names = "--stream", required = true) private String stream;
    @Option(names = "--version", required = true) private int version;
    @Option(names = "--cascade") private boolean cascade;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      if (cascade) {
        List<Key<? extends Specification>> result = new ArrayList<>();
        result.addAll(client.getConsumerBindingKeys(domain, stream, version, null, null, null));
        result.addAll(client.getConsumerKeys(domain, stream, version, null, null));
        result.addAll(client.getProducerBindingKeys(domain, stream, version, null, null, null));
        result.addAll(client.getProducerKeys(domain, stream, version, null, null));
        result.addAll(client.getStreamBindingKeys(domain, stream, version, null, null));
        StreamAndSchemaDiscoverer discoverer = new StreamAndSchemaDiscoverer(client);
        Map<StreamKey, Optional<SchemaKey>> streams = discoverer.discover(domain, stream, version);
        result.addAll(streams.keySet());
        result.addAll(streams.values().stream().flatMap(Optional::stream).collect(toSet()));
        return unmodifiableList(result);
      }
      return unmodifiableList(new ArrayList<>(client.getStreamKeyWithSchemaKeys(domain, stream, version, null, null)
          .keySet()));
    }
  }

  @Command(name = "zone")
  static class Zone extends Base {
    @Option(names = "--zone", required = true) private String zone;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      return unmodifiableList(client.getZoneKeys(zone));
    }
  }

  @Command(name = "infrastructure")
  static class Infrastructure extends Base {
    @Option(names = "--zone", required = true) private String zone;
    @Option(names = "--infrastructure", required = true) private String infrastructure;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      return unmodifiableList(client.getInfrastructureKeys(zone, infrastructure));
    }
  }

  @Command(name = "producer")
  static class Producer extends Base {
    @Option(names = "--domain", required = true) private String domain;
    @Option(names = "--stream", required = true) private String stream;
    @Option(names = "--version", required = true) private int version;
    @Option(names = "--zone", required = true) private String zone;
    @Option(names = "--producer", required = true) private String producer;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      return unmodifiableList(client.getProducerKeys(domain, stream, version, zone, producer));
    }
  }

  @Command(name = "consumer")
  static class Consumer extends Base {
    @Option(names = "--domain", required = true) private String domain;
    @Option(names = "--stream", required = true) private String stream;
    @Option(names = "--version", required = true) private int version;
    @Option(names = "--zone", required = true) private String zone;
    @Option(names = "--consumer", required = true) private String consumer;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      return unmodifiableList(client.getConsumerKeys(domain, stream, version, zone, consumer));
    }
  }

  @Command(name = "streamBinding")
  static class StreamBinding extends Base {
    @Option(names = "--domain", required = true) private String domain;
    @Option(names = "--stream", required = true) private String stream;
    @Option(names = "--version", required = true) private int version;
    @Option(names = "--zone", required = true) private String zone;
    @Option(names = "--infrastructure", required = true) private String infrastructure;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      return unmodifiableList(client.getStreamBindingKeys(domain, stream, version, zone, infrastructure));
    }
  }

  @Command(name = "producerBinding")
  static class ProducerBinding extends Base {
    @Option(names = "--domain", required = true) private String domain;
    @Option(names = "--stream", required = true) private String stream;
    @Option(names = "--version", required = true) private int version;
    @Option(names = "--zone", required = true) private String zone;
    @Option(names = "--infrastructure", required = true) private String infrastructure;
    @Option(names = "--producer", required = true) private String producer;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      return unmodifiableList(client.getProducerBindingKeys(domain, stream, version, zone, infrastructure, producer));
    }
  }

  @Command(name = "consumerBinding")
  static class ConsumerBinding extends Base {
    @Option(names = "--domain", required = true) private String domain;
    @Option(names = "--stream", required = true) private String stream;
    @Option(names = "--version", required = true) private int version;
    @Option(names = "--zone", required = true) private String zone;
    @Option(names = "--infrastructure", required = true) private String infrastructure;
    @Option(names = "--consumer", required = true) private String consumer;

    @Override
    List<Key<? extends Specification>> findEntities(EntityClient client) {
      return unmodifiableList(client.getConsumerBindingKeys(domain, stream, version, zone, infrastructure, consumer));
    }
  }
}
