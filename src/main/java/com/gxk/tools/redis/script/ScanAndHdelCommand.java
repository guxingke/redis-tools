package com.gxk.tools.redis.script;

import com.gxk.tools.redis.core.Command;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;

// https://redis.io/commands/scan/
// https://redis.io/commands/hdel/

// uri pattern
// redis://127.0.0.1:6379 * fields
public class ScanAndHdelCommand implements Command {

  @Override
  public void handle(String... args) {
    if (args.length < 3) {
      System.out.println(".. uri pattern");
      return;
    }

    boolean more = true;
    var cursor = "0";
    ScanParams params = new ScanParams();
    params.match(args[1]);
    params.count(1000);

    var fields = new String[args.length - 2];
    System.arraycopy(args, 2, fields, 0, fields.length);
    try (var cli = new Jedis(args[0])) {
      while (more) {
        var sr = cli.scan(cursor, params);
        cursor = sr.getCursor();

        var result = sr.getResult();
        for (String line : result) {
          cli.hdel(line, fields);
          System.out.println(line);
        }

        more = !cursor.equals("0");
      }
    }
  }
}
