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
package com.expediagroup.streamplatform.streamregistry.core.validator;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.model.Stream;

@RunWith(MockitoJUnitRunner.class)
public class ConfiguredEntityValidatorTest {
  @Mock
  private NameValidator nameValidator;

  private ConfiguredEntityValidator underTest = new ConfiguredEntityValidator();

  @Test
  public void valid() {
    Stream entity = Stream
        .builder()
        .type("type")
        .configuration(Map.of())
        .build();

    Optional<Stream> existing = Optional.of(Stream
        .builder()
        .type("type")
        .configuration(Map.of())
        .build());

    underTest.validate(entity, existing);
  }

  @Test(expected = NullPointerException.class)
  public void nullType() {
    Stream entity = Stream
        .builder()
        .type(null)
        .configuration(Map.of())
        .build();

    Optional<Stream> existing = Optional.empty();

    underTest.validate(entity, existing);
  }

  @Test(expected = NullPointerException.class)
  public void nullConfiguration() {
    Stream entity = Stream
        .builder()
        .type("type")
        .configuration(null)
        .build();

    Optional<Stream> existing = Optional.empty();

    underTest.validate(entity, existing);
  }

  @Test(expected = IllegalArgumentException.class)
  public void changedType() {
    Stream entity = Stream
        .builder()
        .type("type1")
        .configuration(Map.of())
        .build();

    Optional<Stream> existing = Optional.of(Stream
        .builder()
        .type("type2")
        .configuration(Map.of())
        .build());

    underTest.validate(entity, existing);
  }
}
