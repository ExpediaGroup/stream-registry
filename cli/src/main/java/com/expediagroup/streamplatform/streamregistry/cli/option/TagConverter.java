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
package com.expediagroup.streamplatform.streamregistry.cli.option;

import picocli.CommandLine.ITypeConverter;

import com.expediagroup.streamplatform.streamregistry.state.model.specification.Tag;

public class TagConverter implements ITypeConverter<Tag> {
  @Override
  public Tag convert(String value) {
    String[] split = value.split(":");
    return new Tag(split[0], split[1]);
  }
}
