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

import java.time.Clock;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.repositories.SessionRepository;
import com.expediagroup.streamplatform.streamregistry.core.security.Credentials;
import com.expediagroup.streamplatform.streamregistry.core.security.CredentialsGenerator;
import com.expediagroup.streamplatform.streamregistry.model.Session;

@Component
public class SessionService {

  private final SessionRepository sessionRepository;
  private final CredentialsGenerator credentialsGenerator;
  private final Clock clock;
  private final long sessionExpirationInMs;

  @Autowired
  public SessionService(SessionRepository sessionRepository, CredentialsGenerator credentialsGenerator, Clock clock,
                        @Value("${session-expiration-in-ms}") long sessionExpirationInMs) {
    this.sessionRepository = sessionRepository;
    this.credentialsGenerator = credentialsGenerator;
    this.clock = clock;
    this.sessionExpirationInMs = sessionExpirationInMs;
  }

  public Session create(Session session) throws ValidationException {
    Credentials credentials = credentialsGenerator.generate();
    session.setId(credentials.getId());
    session.setSecret(credentials.getSecret());
    session.setExpiresAt(sessionExpiration());
    return sessionRepository.save(session);
  }

  public Session renew(String id) throws ValidationException {
    Optional<Session> existing = sessionRepository.findById(id);
    if (existing.isEmpty()) {
      throw new ValidationException("Can't rename session because it doesn't exist");
    }
    Session session = existing.get();
    if (session.getExpiresAt() > clock.millis()) {
      session.setExpiresAt(sessionExpiration());
      return sessionRepository.save(session);
    } else {
      throw new ValidationException("The session is expired. Please create a new one to access the resource.");
    }
  }

  private long sessionExpiration() {
    return clock.millis() + sessionExpirationInMs;
  }
}
