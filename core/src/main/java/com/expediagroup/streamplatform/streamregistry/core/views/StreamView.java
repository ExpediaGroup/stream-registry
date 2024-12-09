/**
 * Copyright (C) 2018-2024 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.views;

import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.expediagroup.streamplatform.streamregistry.model.Stream;
import com.expediagroup.streamplatform.streamregistry.model.keys.StreamKey;
import com.expediagroup.streamplatform.streamregistry.repository.StreamRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StreamView {
  private final StreamRepository streamRepository;

  public Optional<Stream> get(StreamKey key) {
    return streamRepository.findById(key);
  }

  public java.util.stream.Stream<Stream> findAll(Predicate<Stream> filter) {
    return streamRepository.findAll().stream().filter(filter);
  }

  public boolean exists(StreamKey key) {
    return get(key).isPresent();
  }
}
