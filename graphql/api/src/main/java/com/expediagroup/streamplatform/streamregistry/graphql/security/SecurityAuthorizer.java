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
package com.expediagroup.streamplatform.streamregistry.graphql.security;

import java.util.function.Predicate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component("security")
@RequiredArgsConstructor
public class SecurityAuthorizer {

  private final BeanFactory beanFactory;

  public boolean authorize() {
    ObjectProvider<AuthorizationProvider> authorizationProviderObjectProvider = beanFactory
        .getBeanProvider(AuthorizationProvider.class);

    AuthorizationProvider authorizationProvider = authorizationProviderObjectProvider
        .getIfAvailable(() -> () -> securityContext -> true);

    Predicate<SecurityContext> provide = authorizationProvider.provide();
    SecurityContext securityContext = SecurityContextHolder.getContext();
    boolean isAccessGranted = provide.test(securityContext);
    log.info("Authorization isAccessGranted: {}", isAccessGranted);
    return isAccessGranted;
  }

  public interface AuthorizationProvider {

    Predicate<SecurityContext> provide();
  }

  @Getter
  @Setter
  public static class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements
      MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;
    private Object target;

    public CustomMethodSecurityExpressionRoot(
        Authentication authentication) {
      super(authentication);
    }

    public void setThis(Object target) {
      this.target = target;
    }

    @Override
    public Object getThis() {
      return target;
    }
  }
}
