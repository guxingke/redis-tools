package com.gxk.tools.redis.script;

import com.gxk.tools.redis.core.Command;
import java.util.stream.IntStream;
import redis.clients.jedis.Jedis;

/**
 * 迁移 slots， 仅原 slot 所在节点宕机使用
 *
 * mig [uri] [min] [max]
 *
 * mig redis://10.200.16.65:6879 min max
 */
public class ClusterSlotMigCmd implements Command {

  @Override
  public void handle(String... args) {
    if (args.length < 2) {
      System.out.println(".. uri min [max]");
      return;
    }

    var min = Integer.parseInt(args[1]);
    var max = min;
    if (args.length > 2) {
      max = Integer.parseInt(args[2]);
    }

    try (var cli = new Jedis(args[0])) {
      var slots = IntStream.range(min, max + 1).toArray();
      cli.clusterDelSlots(slots);
      cli.clusterAddSlots(slots);
      for (int i = min; i < max; i++) {
        cli.clusterSetSlotStable(i);
      }
      cli.clusterBumpEpoch();
    }
  }
}
