package com.gxk.tools.redis.cli;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "test", hidden = true)
public class TestCmd implements Callable<Integer> {

  @Option(names = "-x")
  int x;

  @Override
  public Integer call() { // business logic
    System.out.printf("x=%s%n", x);
    return 123; // exit code
  }
}
