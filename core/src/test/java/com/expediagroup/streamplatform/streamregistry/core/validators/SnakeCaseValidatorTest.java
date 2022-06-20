/**
 * Copyright (C) 2018-2022 Expedia, Inc.
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
package com.expediagroup.streamplatform.streamregistry.core.validators;

import static com.expediagroup.streamplatform.streamregistry.core.validators.SnakeCaseValidator.validate;

import org.junit.Test;

public class SnakeCaseValidatorTest {

  @Test
  public void validName() {
    validate("abc");
  }

  @Test
  public void validNameWithTrailingDigit() {
    validate("abc1");
  }

  @Test
  public void validNameWithUnderscore() {
    validate("a_bc_1");
  }

  @Test
  public void validNameWithUnderscores() {
    validate("abc_def_1");
  }

  @Test
  public void validNameWithInternalDigit() {
    validate("a1b");
  }

  @Test(expected = ValidationException.class)
  public void invalidBeginsWithDigit() {
    validate("1abc_def_1");
  }

  @Test(expected = ValidationException.class)
  public void invalidBeginsWithUnderscore() {
    validate("_abc_def_1");
  }

  @Test(expected = ValidationException.class)
  public void invalidEndsWithUnderscore() {
    validate("abc_def_");
  }

  @Test(expected = ValidationException.class)
  public void invalidConsecutiveUnderscores() {
    validate("abc__def_1");
  }

  @Test(expected = ValidationException.class)
  public void invalidLeadingWhitespace() {
    validate(" abc");
  }

  @Test(expected = ValidationException.class)
  public void invalidTrailingWhitespace() {
    validate("abc ");
  }

  @Test(expected = ValidationException.class)
  public void invalidInternalWhitespace() {
    validate("a bc");
  }

  @Test(expected = ValidationException.class)
  public void invalidCapitals() {
    validate("aBc");
  }

  @Test(expected = ValidationException.class)
  public void invalidHyphen() {
    validate("abc-def");
  }
}
