package com.gxk.tools.redis.script;

import com.gxk.tools.redis.core.Command;

public class TestCommand implements Command {

  @Override
  public void handle(String... args) {
    System.out.println("hhhhhh");
  }
}
