package com.expediagroup.streamplatform.streamregistry.repository.kafka;

import com.expediagroup.streamplatform.streamregistry.state.EntityView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.expediagroup.streamplatform.streamregistry.repository.kafka.SampleState.domainSpecificationDeletionEvent;
import static com.expediagroup.streamplatform.streamregistry.repository.kafka.SampleState.domainSpecificationEvent;
import static com.expediagroup.streamplatform.streamregistry.repository.kafka.SampleState.domainStatusDeletionEvent;
import static com.expediagroup.streamplatform.streamregistry.repository.kafka.SampleState.domainStatusEvent;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(MockitoJUnitRunner.class)
public class PurgingEntityViewListenerTest {

  @Mock
  private EntityView view;
  private PurgingEntityViewListener purgingEntityViewListener;
  
  @Before
  public void before() {
    purgingEntityViewListener = new PurgingEntityViewListener(view);
  }

  @Test
  public void purgeDeleteEvents() {
    purgingEntityViewListener.onEvent(null, domainSpecificationDeletionEvent());

    verify(view).purgeDeleted(domainSpecificationDeletionEvent().getKey());
  }

  @Test
  public void ignoreNonDeleteEvents() {
    purgingEntityViewListener.onEvent(null, domainSpecificationEvent());
    purgingEntityViewListener.onEvent(null, domainStatusDeletionEvent());
    purgingEntityViewListener.onEvent(null, domainStatusEvent());

    verifyNoInteractions(view);
  }
}