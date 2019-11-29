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
package com.expediagroup.streamplatform.streamregistry.core.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.core.repositories.SessionRepository;
import com.expediagroup.streamplatform.streamregistry.core.security.SecretGenerator;
import com.expediagroup.streamplatform.streamregistry.model.Session;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceTest {

  private static final String TEST_SESSION_ID = "test-session-id";
  private static final String TEST_SECRET = "test-secret";
  @Mock
  private SessionRepository sessionRepository;
  @Mock
  private SecretGenerator secretGenerator;
  @Mock
  private Clock clock;

  private SessionService sessionService;

  @Before
  public void before() {
    this.sessionService = new SessionService(sessionRepository, secretGenerator, clock, 1);
  }

  @Test
  public void shouldSuccessfullySaveSession() {
    Session session = new Session();
    when(secretGenerator.generate()).thenReturn(TEST_SESSION_ID).thenReturn(TEST_SECRET);
    when(sessionRepository.save(any(Session.class))).thenReturn(session);

    ArgumentCaptor<Session> sessionArgumentCaptor = ArgumentCaptor.forClass(Session.class);

    sessionService.create(session);

    verify(sessionRepository).save(sessionArgumentCaptor.capture());

    Session savedSession = sessionArgumentCaptor.getValue();
    assertEquals(TEST_SESSION_ID, savedSession.getId());
    assertEquals(TEST_SECRET, savedSession.getSecret());
  }

  @Test
  public void successfulSessionRenewal() {
    Session session = new Session();
    session.setId(TEST_SESSION_ID);
    session.setSecret(TEST_SECRET);
    session.setExpiresAt(1L);

    when(sessionRepository.findById(anyString())).thenReturn(Optional.of(session));
    when(sessionRepository.save(session)).thenReturn(session);
    when(clock.millis()).thenReturn(0L).thenReturn(2L);

    Session renewedSession = sessionService.renew(TEST_SESSION_ID, TEST_SECRET).get();

    assertEquals(Long.valueOf(3L), renewedSession.getExpiresAt());
  }

  @Test(expected = ValidationException.class)
  public void failForExpiredSession() {
    Session session = new Session();
    session.setId(TEST_SESSION_ID);
    session.setSecret(TEST_SECRET);
    session.setExpiresAt(0L);

    when(sessionRepository.findById(anyString())).thenReturn(Optional.of(session));
    when(clock.millis()).thenReturn(1L);

    sessionService.renew(TEST_SESSION_ID, TEST_SECRET);
  }

  @Test(expected = ValidationException.class)
  public void failRenewForMissingSession() {
    when(sessionRepository.findById(anyString())).thenReturn(Optional.empty());

    sessionService.renew(TEST_SESSION_ID, TEST_SECRET);
  }
}