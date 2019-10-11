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
package com.expediagroup.streamplatform.streamregistry.app.inputs;

import static com.expediagroup.streamplatform.streamregistry.model.scalars.ObjectNodeMapper.serialise;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.databind.node.ObjectNode;

import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Tag;

@Data
@Builder
public class SpecificationInput {

  String description = null;
  List<TagInput> tags = null;
  String type = null;
  ObjectNode configuration = null;

  public Specification asSpecification() {
    return new Specification(
        getDescription(),
        getType(),
        serialise(getConfiguration()),
        tags()
    );
  }

  private List<Tag> tags() {
    List out = new ArrayList<>();
    if (tags != null) {
      for (TagInput t : tags) {
        out.add(new Tag(t.getName(), t.getValue()));
      }
    }
    return out;
  }
}