package com.expediagroup.streamplatform.streamregistry.graphql.filters;

import static com.expediagroup.streamplatform.streamregistry.graphql.filters.TagMatchUtility.matchesTag;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.graphql.model.queries.TagQuery;
import com.expediagroup.streamplatform.streamregistry.model.Tag;

public class TagMatchUtilityTest {

  @Test
  public void matchesTag_nameAndValue() {
    assertTrue(matchesTag(
        new Tag("name", "value"),
        TagQuery.builder().nameRegex("name").valueRegex("value").build()
    ));
  }

  @Test
  public void matchesTag_nameOnly() {
    assertTrue(matchesTag(
        new Tag("name", null),
        TagQuery.builder().nameRegex("name").build()
    ));
  }

  @Test
  public void matchesTag_valueDoesNotMatch() {
    assertFalse(matchesTag(
        new Tag("name", "value"),
        TagQuery.builder().nameRegex("name").valueRegex("x").build()
    ));
  }

  @Test
  public void matchesTag_nameDoesNotMatch() {
    assertFalse(matchesTag(
        new Tag("name", null),
        TagQuery.builder().nameRegex("x").build()
    ));
  }
}
