package com.gxk.tools.redis.core;

import java.util.HashMap;
import java.util.Map;

public abstract class Registry {

  private static Map<String, Class<? extends Command>> HOLDER = new HashMap<>();

  public static void register(
      String name,
      Class<? extends Command> handler
  ) {
    HOLDER.put(name, handler);
  }

  public static Command lookup(String name) {
    Class<? extends Command> cls = HOLDER.get(name);
    try {
      return cls.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      System.err.println("not found valid handler or init handler failed, handler name " + name);
      return null;
    }
  }
}
