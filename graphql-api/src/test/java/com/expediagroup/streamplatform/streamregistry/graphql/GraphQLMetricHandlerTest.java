package com.expediagroup.streamplatform.streamregistry.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Duration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.DomainKeyInput;
import com.expediagroup.streamplatform.streamregistry.graphql.query.DomainQuery;
import com.expediagroup.streamplatform.streamregistry.graphql.query.impl.DomainQueryImpl;
import com.expediagroup.streamplatform.streamregistry.model.Domain;

@RunWith(MockitoJUnitRunner.class)
public class GraphQLMetricHandlerTest {
  @Mock
  private Domain domain;
  @Mock
  private DomainQueryImpl delegate;
  @Mock
  private MeterRegistry registry;//= new SimpleMeterRegistry();
  @Mock
  private Timer timer;

  private final DomainKeyInput key = DomainKeyInput.builder().build();
  private final Tags tags = Tags
      .of("api", "DomainQuery")
      .and("method", "byKey");

  private Method method;
  private GraphQLMetricHandler underTest;

  @Before
  public void before() throws Exception {
    when(registry.timer(any(String.class), any(Tags.class))).thenReturn(timer);

    method = DomainQuery.class.getDeclaredMethod("byKey", DomainKeyInput.class);
    underTest = new GraphQLMetricHandler(delegate, registry);
  }

  @Test
  public void success() throws Throwable {
    when(delegate.byKey(key)).thenReturn(domain);

    Object result = underTest.invoke(null, method, new Object[]{key});
    assertThat(result, is(domain));

    verify(registry).timer("graphql_api", tags.and("result", "success"));
    verify(timer).record(any(Duration.class));
  }

  @Test
  public void failure() throws Throwable {
    RuntimeException failed = new RuntimeException("failed");
    when(delegate.byKey(key)).thenThrow(failed);

    try {
      underTest.invoke(null, method, new Object[]{key});
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertThat(e, is(failed));
    }

    verify(registry).timer("graphql_api", tags.and("result", "failure"));
    verify(timer).record(any(Duration.class));
  }

}
