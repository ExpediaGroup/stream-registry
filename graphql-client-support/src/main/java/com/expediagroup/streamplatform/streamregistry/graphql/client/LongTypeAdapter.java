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
package com.expediagroup.streamplatform.streamregistry.graphql.client;


import java.math.BigDecimal;

import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;

public class LongTypeAdapter implements CustomTypeAdapter<Object> {
  @Override
  public CustomTypeValue encode(Object value) {
    if (!(value instanceof Long)) {
      throw new IllegalArgumentException("Expected 'Long', got: " + value);
    }
    return CustomTypeValue.fromRawValue(value);
  }

  @Override
  public Object decode(CustomTypeValue value) {
    return ((BigDecimal) value.value).longValue();
  }
}
