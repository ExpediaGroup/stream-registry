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
package com.expediagroup.streamplatform.streamregistry.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.handler.HandlerWrapper;
import com.expediagroup.streamplatform.streamregistry.core.predicate.EntityPredicateFactory;
import com.expediagroup.streamplatform.streamregistry.core.validator.EntityValidator;
import com.expediagroup.streamplatform.streamregistry.model.Domain;
import com.expediagroup.streamplatform.streamregistry.repository.Repository;

@RunWith(MockitoJUnitRunner.class)
public class DomainServiceTest {
  @Mock
  private EntityValidator entityValidator;
  @Mock
  private HandlerWrapper<Domain> domainHandler;
  @Mock
  private Repository<Domain, Domain.Key> domainRepository;
  @Mock
  private EntityPredicateFactory entityPredicateFactory;
  @Mock
  private Stream<Domain> stream1;
  @Mock
  private Stream<Domain> stream2;
  @Mock
  private Predicate<Domain> predicate;

  private DomainService underTest;

  @Before
  public void before() {
    underTest = new DomainService(entityValidator, domainHandler, domainRepository, entityPredicateFactory);
  }

  @Test
  public void upsert() {
    Domain domain = Domain.builder().name("name").build();
    Optional<Domain> existing = Optional.empty();

    when(domainRepository.get(domain.key())).thenReturn(existing);
    when(domainHandler.handle(domain, existing)).thenReturn(domain);

    underTest.upsert(domain);

    InOrder inOrder = inOrder(entityValidator, domainHandler, domainRepository);
    inOrder.verify(domainRepository).get(domain.key());
    inOrder.verify(entityValidator).validate(domain, existing);
    inOrder.verify(domainHandler).handle(domain, existing);
    inOrder.verify(domainRepository).upsert(domain);
  }

  @Test
  public void getExisting() {
    Domain domain = Domain.builder().name("name").build();
    Optional<Domain> existing = Optional.of(domain);

    when(domainRepository.get(domain.key())).thenReturn(existing);

    Domain result = underTest.get(domain.key());

    assertThat(result, is(domain));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getNotExisting() {
    Domain domain = Domain.builder().name("name").build();
    Optional<Domain> existing = Optional.empty();

    when(domainRepository.get(domain.key())).thenReturn(existing);

    underTest.get(domain.key());
  }

  @Test
  public void stream() {
    Domain query = Domain.builder().build();

    when(domainRepository.stream()).thenReturn(stream1);
    when(entityPredicateFactory.create(query)).thenReturn(predicate);
    when(stream1.filter(predicate)).thenReturn(stream2);

    Stream<Domain> result = underTest.stream(query);

    assertThat(result, is(stream2));
  }
}
