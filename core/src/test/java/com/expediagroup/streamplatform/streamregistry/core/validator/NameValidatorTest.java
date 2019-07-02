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
package com.expediagroup.streamplatform.streamregistry.core.validator;

import org.junit.Test;

public class NameValidatorTest {

  private NameValidator underTest = new NameValidator();

  @Test
  public void valid() {
    underTest.validate("abc1.def2-ghi");
  }

  @Test(expected = IllegalArgumentException.class)
  public void doubleHyphen() {
    underTest.validate("abc1.def2--ghi");
  }

  @Test(expected = IllegalArgumentException.class)
  public void doubleDot() {
    underTest.validate("abc1..def2-ghi");
  }

  @Test(expected = IllegalArgumentException.class)
  public void hyphenDot() {
    underTest.validate("abc1.def2-.ghi");
  }

  @Test(expected = IllegalArgumentException.class)
  public void dotHyphen() {
    underTest.validate("abc1.def2.-ghi");
  }

  @Test(expected = IllegalArgumentException.class)
  public void startDigit() {
    underTest.validate("1abc1.def2-ghi");
  }

  @Test(expected = IllegalArgumentException.class)
  public void dotDigit() {
    underTest.validate("abc1.1def2-ghi");
  }

  @Test(expected = IllegalArgumentException.class)
  public void hyphenDigit() {
    underTest.validate("abc1.def2-1ghi");
  }

  @Test(expected = IllegalArgumentException.class)
  public void endDot() {
    underTest.validate("abc1.def2-ghi.");
  }

  @Test(expected = IllegalArgumentException.class)
  public void endHyphen() {
    underTest.validate("abc1.def2-ghi-");
  }
}
