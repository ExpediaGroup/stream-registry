package com.expediagroup.streamplatform.streamregistry.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import io.micrometer.core.instrument.MeterRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.expediagroup.streamplatform.streamregistry.graphql.query.impl.DomainQueryImpl;

@RunWith(MockitoJUnitRunner.class)
public class GraphQLBeanPostProcessorTest {
  @Mock
  private ApplicationContext applicationContext;
  @Mock
  private MeterRegistry registry;

  private final GraphQLBeanPostProcessor underTest = new GraphQLBeanPostProcessor();

  @Before
  public void before() {
    underTest.setApplicationContext(applicationContext);
  }

  @Test
  public void notGraphQLApiType() {
    Object bean = "notGraphQLApiType";
    Object result = underTest.postProcessAfterInitialization(bean, "beanName");
    assertThat(result, is(bean));
  }

  @Test
  public void graphQLApiType() {
    when(applicationContext.getBean(MeterRegistry.class)).thenReturn(registry);
    Object bean = new DomainQueryImpl(null);
    Object result = underTest.postProcessAfterInitialization(bean, "beanName");

    GraphQLMetricHandler handler = (GraphQLMetricHandler) Proxy.getInvocationHandler(result);
    assertThat(handler.getDelegate(), is(bean));
    assertThat(handler.getRegistry(), is(registry));
  }
}
