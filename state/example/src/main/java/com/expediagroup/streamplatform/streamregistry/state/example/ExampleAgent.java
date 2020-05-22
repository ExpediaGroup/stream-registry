package com.expediagroup.streamplatform.streamregistry.state.example;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityViewListener;
import com.expediagroup.streamplatform.streamregistry.state.EventSender;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity.DomainKey;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.DefaultSpecification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class ExampleAgent implements EntityViewListener {
  private static final ObjectMapper mapper = new ObjectMapper();

  private final EventSender eventSender;
  private final EntityView entityView;

  @PostConstruct
  void init() {
    //Commence bootstrapping the Stream Registry state
    CompletableFuture<Void> future = entityView.load(this);

    //Block until the Stream Registry state is fully loaded
    future.join();
  }

  //Perform a full reconciliation on a schedule
  @Scheduled(initialDelayString = "${initialDelay:PT60S}", fixedDelayString = "${fixedDelay:PT60S}")
  void reconcile() {
    //Query specific entities
    Optional<Entity<DomainKey, DefaultSpecification>> optionalDomain = entityView.get(new DomainKey("my_domain"));

    //Query all entities of a specific type
    Stream<Entity<DomainKey, DefaultSpecification>> allDomains = entityView.all(DomainKey.class);

    //Mutate an entity
    CompletableFuture<Void> future = eventSender.send(Event.of(
        new DomainKey("my_domain"),
        new DefaultSpecification(
            "description",
            List.of(new Tag("name", "value")),
            "type",
            mapper.createObjectNode()
        )
    ));

    //Block until sent
    future.join();
  }

  @Override
  public <K extends Entity.Key<S>, S extends Specification> void onEvent(Entity<K, S> oldEntity, Event<K, S> event) {
    //React to mutations as they occur
  }
}
