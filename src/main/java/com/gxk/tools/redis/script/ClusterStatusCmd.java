package com.gxk.tools.redis.script;

import com.gxk.tools.redis.core.Command;
import java.util.Arrays;
import redis.clients.jedis.Jedis;

/**
 *
 * 集群状态
 *
 * status [uri]
 *
 * status redis://10.200.16.65:6879
 */
public class ClusterStatusCmd implements Command {

  @Override
  public void handle(String... args) {
    if (args.length < 1) {
      System.out.println(".. uri");
      return;
    }

    try (var cli = new Jedis(args[0])) {
      System.out.println();

      var rn = cli.clusterNodes();

      int nc = 0;
      int nmc = 0;
      var ns = rn.split("\n");

      for (String n : ns) {
        nc++;

        var items = n.split(" ");

        //8fe43ca11a01c1608f9a2556e2d795a6c8814a7e 10.200.16.64:6879@16879 master,nofailover - 0 1656489740000 8 connected 0-202 10923-14865 14872-1490
        var id = items[0];
        var node = items[1];
        var st = items[2];
        if (st.startsWith("master")) {
          nmc++;
        }
        // 3
        // 4
        // 5
        // 6
        // 7
        int sc = 0;
        var sa = new String[items.length - 8];
        System.arraycopy(items, 8, sa, 0, sa.length);

        for (String s : sa) {
          if (s.contains("-")) {
            var sss = s.split("-");
            sc = sc + (Integer.parseInt(sss[1]) - Integer.parseInt(sss[0])) + 1;
          } else {
            sc++;
          }
        }

        System.out.println("%d slots %s %s %s %s".formatted(sc, id, node, st, Arrays.toString(sa)));
      }

      System.out.println("%d with %d master".formatted(nc, nmc));
    }

    System.out.println();
  }
}
