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
package com.expediagroup.streamplatform.streamregistry.graphql.model.inputs;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class NameNormaliser {
  private static final Pattern pattern = Pattern.compile("^[a-z][a-z0-9]*(?:_[a-z0-9]+)*$");

  public static String normalise(String name) {
    name = name.trim().toLowerCase();
    if (!pattern.matcher(name).matches()) {
      throw new IllegalArgumentException(String.format("Invalid name '%s' must be conform to pattern %s", name, pattern.pattern()));
    }
    return name;
  }
}
