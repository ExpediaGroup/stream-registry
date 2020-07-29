package com.expediagroup.streamplatform.streamregistry.cli;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.expediagroup.streamplatform.streamregistry.cli.command.Apply;
import com.expediagroup.streamplatform.streamregistry.cli.command.Delete;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.UnmatchedArgumentException;

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
