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

import static lombok.AccessLevel.PACKAGE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Stopwatch;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

@Slf4j
@RequiredArgsConstructor
@Getter(PACKAGE)
class GraphQLMetricHandler implements InvocationHandler {
  private final Object delegate;
  private final MeterRegistry registry;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    boolean success = false;
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      Object result = method.invoke(delegate, args);
      success = true;
      return result;
    } catch (InvocationTargetException e) {
      if (e.getCause() != null) {
        throw e.getCause();
      }
      throw e;
    } finally {
      Tags tags = Tags
          .of("api", method.getDeclaringClass().getSimpleName())
          .and("method", method.getName())
          .and("result", success ? "success" : "failure");
      registry.timer("graphql_api", tags).record(stopwatch.elapsed());
    }
  }
}
