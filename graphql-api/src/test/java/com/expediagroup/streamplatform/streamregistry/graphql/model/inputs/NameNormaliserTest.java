package com.expediagroup.streamplatform.streamregistry.graphql.model.inputs;

import static com.expediagroup.streamplatform.streamregistry.graphql.model.inputs.NameNormaliser.normalise;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class NameNormaliserTest {
  @Test
  public void validName() {
    assertThat(normalise("abc_def_1"), is("abc_def_1"));
  }

  @Test
  public void validNameWithWhitespace() {
    assertThat(normalise(" abc_def_1 "), is("abc_def_1"));
  }

  @Test
  public void validNameUppercase() {
    assertThat(normalise("ABC_DEF_1"), is("abc_def_1"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void beginsWithDigit() {
    normalise("1abc_def_1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void beginsWithUnderscore() {
    normalise("_abc_def_1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void endsWithUnderscore() {
    normalise("abc_def_");
  }
}
