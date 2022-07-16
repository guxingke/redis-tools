package com.gxk.tools.redis.cli;

import com.gxk.tools.redis.rdb.Entry;
import com.gxk.tools.redis.rdb.KeyValuePair;
import com.gxk.tools.redis.rdb.RdbParser;
import com.gxk.tools.redis.utils.Tools;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "rdb-scan", description = "Like redis-cli scan command, but for rdb.")
public class RdbScanCmd implements Runnable {

  @Parameters(index = "0")
  File rdb;

  @Option(names = {"PATTERN", "-p"}, defaultValue = "*", arity = "0..*")
  List<String> patterns;

  @Option(names = {"Type", "-t"}, arity = "0..*")
  List<String> cmds;

  @Override
  public void run() {

    var ps = patterns.stream().map(it -> it.getBytes(StandardCharsets.US_ASCII)).toList();

    int total = 0;
    int hit = 0;
    byte[] key = null;
    try (var p = new RdbParser(rdb)) {
      Entry e;
      while ((e = p.readNext()) != null) {
        switch (e.getType()) {
          case KEY_VALUE_PAIR -> {
            var pair = (KeyValuePair) e;
            total++;

            key = pair.getKey();

            if (cmds != null) {
              boolean match = false;
              for (String cmd : cmds) {
                if (Tools.typeMatch(pair.getValueType(), cmd)) {
                  match = true;
                  break;
                }
              }

              if (!match) {
                return;
              }
            }

            for (byte[] pattern : ps) {
              if (Tools.match(key, pattern)) {
                hit++;
                System.out.println(new String(key));
              }
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println(new String(key));
      e.printStackTrace();
    }

    System.out.printf("\ntotal: %d \nhit: %d%n", total, hit);
  }
}
