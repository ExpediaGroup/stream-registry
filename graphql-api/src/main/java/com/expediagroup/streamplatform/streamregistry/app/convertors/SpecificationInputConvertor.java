package com.expediagroup.streamplatform.streamregistry.app.convertors;
/**
 * Copyright (C) 2016-2019 Expedia Inc.
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


import static com.expediagroup.streamplatform.streamregistry.app.scalars.ObjectNodeMapper.serialise;

import java.util.ArrayList;
import java.util.List;

import com.expediagroup.streamplatform.streamregistry.app.Specification;
import com.expediagroup.streamplatform.streamregistry.app.Tag;
import com.expediagroup.streamplatform.streamregistry.app.inputs.SpecificationInput;
import com.expediagroup.streamplatform.streamregistry.app.inputs.TagInput;

import lombok.Data;

@Data
public class SpecificationInputConvertor {

  public static Specification convert(SpecificationInput specificationInput) {
    return new Specification(
        specificationInput.getDescription(),
        specificationInput.getType(),
        serialise(specificationInput.getConfiguration()),
        getTags(specificationInput)
    );
  }

  private static List<Tag> getTags(SpecificationInput specificationInput) {
    List out = new ArrayList<>();
    if (specificationInput != null && specificationInput.getTags() != null) {
      for (TagInput t : specificationInput.getTags()) {
        out.add(new Tag(t.getName(), t.getValue()));
      }
    }
    return out;
  }
}