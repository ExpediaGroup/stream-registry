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
package com.expediagroup.streamplatform.streamregistry.core.security;

import static junit.framework.TestCase.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

public class UUIDCredentialsGeneratorTest {

  private final Pattern UUID_REGEX = Pattern.compile("([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}");

  @Test
  public void shouldGenerateUUIDSecret() {
    CredentialsGenerator cut = new UUIDCredentialsGenerator();

    Credentials credentials = cut.generate();

    assertTrue(UUID_REGEX.matcher(credentials.getId()).matches());
    assertTrue(UUID_REGEX.matcher(credentials.getSecret()).matches());
  }
}
