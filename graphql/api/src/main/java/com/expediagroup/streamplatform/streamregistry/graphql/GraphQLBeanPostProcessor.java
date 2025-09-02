/**
 * Copyright (C) 2018-2025 Expedia, Inc.
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

import static java.lang.reflect.Proxy.newProxyInstance;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GraphQLBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
  @Setter
  private ApplicationContext applicationContext;

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof GraphQLApiType) {
      MeterRegistry registry = applicationContext.getBean(MeterRegistry.class);
      Class<?>[] interfaces = bean.getClass().getInterfaces();
      GraphQLMetricHandler handler = new GraphQLMetricHandler(bean, registry);
      log.debug("Proxying {} with GraphQLMetricHandler", beanName);
      bean = newProxyInstance(GraphQLMetricHandler.class.getClassLoader(), interfaces, handler);
    }
    return bean;
  }

}
