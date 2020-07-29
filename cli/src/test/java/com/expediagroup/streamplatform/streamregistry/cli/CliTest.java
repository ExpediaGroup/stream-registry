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
package com.expediagroup.streamplatform.streamregistry.cli;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.UnmatchedArgumentException;

import com.expediagroup.streamplatform.streamregistry.cli.command.Apply;
import com.expediagroup.streamplatform.streamregistry.cli.command.Delete;

public class CliTest {
  private final CommandLine underTest = new CommandLine(new Cli());

  @Test
  public void apply() {
    ParseResult result = underTest.parseArgs("apply");
    Object object = result.subcommand().commandSpec().userObject();
    assertThat(object, is(instanceOf(Apply.class)));
  }

  @Test
  public void delete() {
    ParseResult result = underTest.parseArgs("delete");
    Object object = result.subcommand().commandSpec().userObject();
    assertThat(object, is(instanceOf(Delete.class)));
  }

  @Test(expected = UnmatchedArgumentException.class)
  public void unknown() {
    underTest.parseArgs("unknown");
  }
}
