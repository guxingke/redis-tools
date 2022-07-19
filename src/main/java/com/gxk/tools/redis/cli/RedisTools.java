package com.gxk.tools.redis.cli;

import picocli.AutoComplete.GenerateCompletion;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

// @formatter:off
@Command(name = "rdt",
    mixinStandardHelpOptions = true,
    version = "0.0.1",
    description = "redis cli tool set",
    footer = "%nSee 'rdt help <command>' to read about a specific subcommand or concept.",
    subcommands = {
        TestCmd.class,
        RdbInfoCmd.class,
        RdbBigKeysCmd.class,
        MonitorCmd.class,
        RdbScanCmd.class,
        RdbKeySpaceCmd.class,
        RdbMemoryCmd.class,
        ScanCmd.class,
        CommandLine.HelpCommand.class,
        TestJsonCmd.class,
        GenerateCompletion.class,
    }
)
// @formatter:on
public class RedisTools implements Runnable {

  @Spec
  CommandLine.Model.CommandSpec spec;

  public int exec(ParseResult parseResult) {

    init();
    return new CommandLine.RunLast().execute(parseResult);
  }

  private void init() {
  }

  @Override
  public void run() {
    spec.commandLine().usage(System.out);
  }
}
