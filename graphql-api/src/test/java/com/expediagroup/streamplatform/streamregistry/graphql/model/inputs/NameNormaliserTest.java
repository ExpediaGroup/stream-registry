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

  @Test
  public void validDelimitedDigit() {
    assertThat(normalise("abc_1_def"), is("abc_1_def"));
  }


  @Test(expected = IllegalArgumentException.class)
  public void invalidBeginsWithDigit() {
    normalise("1abc_def_1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidBeginsWithUnderscore() {
    normalise("_abc_def_1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidEndsWithUnderscore() {
    normalise("abc_def_");
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidConsecutiveUnderscores() {
    normalise("abc__def_1");
  }
}
