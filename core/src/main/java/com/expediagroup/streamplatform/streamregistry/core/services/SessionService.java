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

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.core.repositories.SessionRepository;
import com.expediagroup.streamplatform.streamregistry.model.Session;

@Component
@RequiredArgsConstructor
public class SessionService {
  private final SessionRepository sessionRepository;

  @Value("${session-expiration-in-ms}")
  private long sessionExpirationInMs;

  public Optional<Session> create(Session session) throws ValidationException {
    session.setId(UUID.randomUUID().toString());
    session.setSecret(UUID.randomUUID().toString());
    session.setExpiresAt(sessionExpiration());
    return Optional.ofNullable(sessionRepository.save(session));
  }

  public Optional<Session> renew(String id, String secret) throws ValidationException {
    Optional<Session> existing = sessionRepository.findById(id);
    if (!existing.isPresent()) {
      throw new ValidationException("Can't update because it doesn't exist");
    }
    Session session = existing.get();
    if (session.getSecret().equals(secret)) {
      if (session.getExpiresAt() > System.currentTimeMillis()) {
        session.setExpiresAt(sessionExpiration());
        return Optional.ofNullable(sessionRepository.save(session));
      } else {
        throw new ValidationException("The session is expired.");
      }
    } else {
      throw new ValidationException("Invalid secret for the session.");
    }
  }

  private long sessionExpiration() {
    return System.currentTimeMillis() + sessionExpirationInMs;
  }
}
