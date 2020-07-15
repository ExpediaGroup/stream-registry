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

import com.expediagroup.streamplatform.streamregistry.cli.command.Apply;
import com.expediagroup.streamplatform.streamregistry.cli.command.Delete;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "streamctl",
    subcommands = {
        Apply.class,
        Delete.class
    },
    mixinStandardHelpOptions = true
)
public class Cli {
  public static void main(String[] args) {
    System.exit(new CommandLine(new Cli()).execute(args));
  }
}
