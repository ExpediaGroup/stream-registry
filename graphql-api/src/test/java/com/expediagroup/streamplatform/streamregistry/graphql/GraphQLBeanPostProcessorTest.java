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
package com.expediagroup.streamplatform.streamregistry.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

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
