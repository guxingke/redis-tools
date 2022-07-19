package com.gxk.tools.redis.cli;

import com.gxk.tools.redis.rdb.AuxField;
import com.gxk.tools.redis.rdb.Entry;
import com.gxk.tools.redis.rdb.RdbParser;
import java.io.File;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

@CommandLine.Command(name = "info", description = "Show rdb basic information, from aux field.")
public class RdbInfoCmd implements Runnable {

  @Parameters(index = "0")
  File file;

  @Override
  public void run() {
    int total = 0;
    try (var p = new RdbParser(file)) {
      Entry e;
      while ((e = p.readNext()) != null) {
        switch (e.getType()) {
          case AUX_FIELD -> {
            var af = (AuxField) e;
            var ak = new String(af.getKey());

            System.out.print(ak);
            System.out.println(" : " + new String(af.getValue()));
          }
          case KEY_VALUE_PAIR -> {
            total++;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.printf("total keys: %d%n", total);
  }
}
