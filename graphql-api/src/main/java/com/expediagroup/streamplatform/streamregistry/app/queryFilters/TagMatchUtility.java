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
package com.expediagroup.streamplatform.streamregistry.app.queryFilters;

import java.util.List;

import com.expediagroup.streamplatform.streamregistry.app.queries.TagQuery;
import com.expediagroup.streamplatform.streamregistry.model.Specification;
import com.expediagroup.streamplatform.streamregistry.model.Tag;

public class TagMatchUtility {

  public static boolean matchesAllTagQueries(Specification specification, List<TagQuery> tagQueries) {
    return matchesAllTagQueries(specification == null ? null : specification.getTags(), tagQueries);
  }

  private static boolean matchesAllTagQueries(List<Tag> tags, List<TagQuery> tagQueries) {
    if (tagQueries == null || tagQueries.isEmpty()) {
      return true;
    }
    if (tags == null || tags.isEmpty()) {
      return false;
    }
    for (TagQuery tagQuery : tagQueries) {
      if (!matchesAnyTag(tags, tagQuery)) {
        return false;
      }
    }
    return true;
  }

  private static boolean matchesAnyTag(List<Tag> tags, TagQuery tagQuery) {
    for (Tag tag : tags) {
      if (matchesTag(tag, tagQuery)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesTag(Tag tag, TagQuery tagQuery) {
    if (tagQuery.getNameRegex() != null && !tag.getName().matches(tagQuery.getNameRegex())) {
      return false;
    }
    return tagQuery.getValueRegex() == null || tag.getValue().matches(tagQuery.getValueRegex());
  }
}
