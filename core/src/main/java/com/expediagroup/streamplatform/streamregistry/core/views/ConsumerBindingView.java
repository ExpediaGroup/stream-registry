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

import com.expediagroup.streamplatform.streamregistry.model.ConsumerBinding;
import com.expediagroup.streamplatform.streamregistry.model.keys.ConsumerBindingKey;
import com.expediagroup.streamplatform.streamregistry.repository.ConsumerBindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ConsumerBindingView {
  private final ConsumerBindingRepository consumerBindingRepository;

  public Optional<ConsumerBinding> get(ConsumerBindingKey key) {
    return consumerBindingRepository.findById(key);
  }

  public Stream<ConsumerBinding> findAll(Predicate<ConsumerBinding> filter) {
    return consumerBindingRepository.findAll().stream().filter(filter);
  }

  public boolean exists(ConsumerBindingKey key) {
    return get(key).isPresent();
  }
}
