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

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.model.Configuration;
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
        .configuration(Configuration
            .builder()
            .type("type")
            .build())
        .build();

    Optional<Stream> existing = Optional.of(Stream
        .builder()
        .configuration(Configuration
            .builder()
            .type("type")
            .build())
        .build());

    underTest.validate(entity, existing);
  }

  @Test(expected = NullPointerException.class)
  public void nullConfiguration() {
    Stream entity = Stream
        .builder()
        .configuration(null)
        .build();

    Optional<Stream> existing = Optional.empty();

    underTest.validate(entity, existing);
  }

  @Test(expected = IllegalArgumentException.class)
  public void changedConfigurationType() {
    Stream entity = Stream
        .builder()
        .configuration(Configuration
            .builder()
            .type("type1")
            .build())
        .build();

    Optional<Stream> existing = Optional.of(Stream
        .builder()
        .configuration(Configuration
            .builder()
            .type("type2")
            .build())
        .build());

    underTest.validate(entity, existing);
  }
}
