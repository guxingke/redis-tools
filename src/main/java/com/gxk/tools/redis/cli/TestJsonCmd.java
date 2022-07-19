package com.gxk.tools.redis.cli;

import com.google.gson.Gson;
import picocli.CommandLine.Command;

@Command(name = "test-json", hidden = true)
public class TestJsonCmd implements Runnable {

  @Override
  public void run() {
    var g = new Gson();

    var v = g.toJson(new Demo("test", 10));
    System.out.println(v);

    var n = g.fromJson(v, Demo.class);
    System.out.println(n);
  }

  static final class Demo {

    Demo() {
    }

    Demo(
        String name,
        Integer value
    ) {
      this.name = name;
      this.value = value;
    }

    private String name;
    private Integer value;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Integer getValue() {
      return value;
    }

    public void setValue(Integer value) {
      this.value = value;
    }


    @Override
    public String toString() {
      return "Demo[" + "name=" + name + ", " + "value=" + value + ']';
    }
  }
}
