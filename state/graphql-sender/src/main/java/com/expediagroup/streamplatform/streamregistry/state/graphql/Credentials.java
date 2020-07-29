/**
 * Copyright (C) 2018-2020 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.state.graphql;

import static kotlin.text.Charsets.UTF_8;

import java.util.Base64;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Credentials {
  private final String userName;
  private final String password;

  public String basic() {
    return Base64.getEncoder().encodeToString((userName + ":" + password).getBytes(UTF_8));
  }
}
