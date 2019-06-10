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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.expediagroup.streamplatform.streamregistry.model.Stream;

@RunWith(MockitoJUnitRunner.class)
public class EntityValidatorTest {
  private final Stream entity = Stream.builder().build();
  private final Optional<Stream> existing = Optional.empty();
  @Mock
  private NameValidator nameValidator;
  private EntityValidator underTest;

  @Before
  public void before() {
    underTest = new EntityValidator(nameValidator);
  }

  @Test
  public void valid() {
    underTest.validate(entity, existing);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalid() {
    doThrow(IllegalArgumentException.class).when(nameValidator).validate(any());
    underTest.validate(entity, existing);
  }
}
