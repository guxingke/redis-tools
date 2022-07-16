package com.gxk.tools.redis.core;

public interface Command {

  void handle(String... args);


}
