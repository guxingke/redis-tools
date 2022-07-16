package com.gxk.tools.redis.cli;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;

// https://redis.io/commands/scan/
// uri pattern
// redis://127.0.0.1:6379 *
@CommandLine.Command(name = "scan", description = "Wrapper for redis-cli scan command")
public class ScanCmd implements Runnable {

  @Parameters(index = "0")
  String uri;

  @Option(names = {"PATTERN", "-p"}, defaultValue = "*")
  String pattern;

  @Option(names = {"COUNT", "-c"}, defaultValue= "1000")
  int count;

  @Override
  public void run() {

    boolean more = true;
    var cursor = "0";
    ScanParams params = new ScanParams();
    params.match(pattern);
    params.count(count);

    try (var cli = new Jedis(uri)) {
      while (more) {
        var sr = cli.scan(cursor, params);
        cursor = sr.getCursor();

        var result = sr.getResult();
        for (String line : result) {
          System.out.println(line);
        }

        more = !cursor.equals("0");
      }
    }
  }
}
