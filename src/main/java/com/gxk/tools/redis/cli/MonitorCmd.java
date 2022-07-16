package com.gxk.tools.redis.cli;

import com.gxk.tools.redis.utils.Tools;
import java.nio.charset.StandardCharsets;
import java.util.List;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisMonitor;

// https://redis.io/commands/monitor/
@CommandLine.Command(name = "monitor", description = "Like redis-cli monitor command, addition with pattern and cmd option.")
public class MonitorCmd implements Runnable {

  @Parameters(index = "0")
  String uri;

  @Option(names = {"PATTERN", "-p"}, defaultValue = "*", arity = "0..*")
  List<String> patterns;

  @Option(names = {"CMD", "-c"}, arity = "0..*")
  List<String> cmds;


  @Override
  public void run() {

    var ps = patterns.stream().map(it -> it.getBytes(StandardCharsets.US_ASCII)).toList();

    try (var cli = new Jedis(uri)) {

      JedisMonitor jm = new JedisMonitor() {
        @Override
        public void onCommand(String key) {
//          1657621157.128243 [0 127.0.0.1:54297] "get" "foo"
//          1657621171.569534 [0 127.0.0.1:54297] "HINCRBY" "foo" "bar" "11"
          var items = key.split(" ");
          if (items.length < 5) {
            // skip
            return;
          }
          var cmd = items[3].replace("\"", "");
          if (cmds != null && !cmds.contains(cmd)) {
            return;
          }

          var k = items[4].replace("\"", "");
          // match
          boolean match = false;
          for (byte[] pattern : ps) {
            if (Tools.match(k.getBytes(), pattern)) {
              match = true;
              break;
            }
          }
          if (!match) {
            return;
          }

          System.out.println("%-20s %-10s raw: %s".formatted(k, cmd, key));
        }
      };
      cli.monitor(jm);
    }
  }
}
