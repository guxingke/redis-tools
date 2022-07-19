package com.gxk.tools.redis;

import com.gxk.tools.redis.cli.RedisTools;
import picocli.CommandLine;

public class CliMain {

  public static void main(String[] args) {
    var app = new RedisTools();
    var cli = new CommandLine(app);
    CommandLine gen = cli.getSubcommands().get("generate-completion");
    gen.getCommandSpec().usageMessage().hidden(true);
    cli.setExecutionStrategy(app::exec).execute(args);
  }

}
