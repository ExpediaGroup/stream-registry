/**
 * Copyright (C) 2018-2021 Expedia, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.streamplatform.streamregistry.core.views;

import com.expediagroup.streamplatform.streamregistry.model.Consumer;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ConsumerView {
  private final ConsumerRepository consumerRepository;

  public Optional<Consumer> get(ConsumerKey key) {
    return consumerRepository.findById(key);
  }

  public Stream<Consumer> findAll(Predicate<Consumer> filter) {
    return consumerRepository.findAll().stream().filter(filter);
  }

  public boolean exists(ConsumerKey key) {
    return get(key).isPresent();
  }
}
