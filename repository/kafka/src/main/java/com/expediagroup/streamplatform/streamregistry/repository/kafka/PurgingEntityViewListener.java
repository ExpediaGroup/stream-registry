package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import com.expediagroup.streamplatform.streamregistry.state.EntityViewListener;
import com.expediagroup.streamplatform.streamregistry.state.model.Entity;
import com.expediagroup.streamplatform.streamregistry.state.model.event.Event;
import com.expediagroup.streamplatform.streamregistry.state.model.event.SpecificationDeletionEvent;
import com.expediagroup.streamplatform.streamregistry.state.model.specification.Specification;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
class PurgingEntityViewListener implements EntityViewListener {
  private final EntityView entityView;

  @Override
  public <K extends Entity.Key<S>, S extends Specification> void onEvent(Entity<K, S> oldEntity, @NonNull Event<K, S> event) {
    if (event instanceof SpecificationDeletionEvent) {
      entityView.purgeDeleted(event.getKey());
    }
  }
}
