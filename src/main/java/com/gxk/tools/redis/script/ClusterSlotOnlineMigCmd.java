package com.gxk.tools.redis.script;

import com.gxk.tools.redis.core.Command;
import java.util.Arrays;
import redis.clients.jedis.Jedis;

/**
 * 迁移 slots, 在线迁移，可选迁移 key
 *
 * https://redis.io/commands/cluster-setslot/
 *
 * 1. Set the destination node slot to importing state using CLUSTER SETSLOT <slot> IMPORTING <source-node-id>.
 * 2. Set the source node slot to migrating state using CLUSTER SETSLOT <slot> MIGRATING <destination-node-id>.
 * 3. Get keys from the source node with CLUSTER GETKEYSINSLOT command and move them into the destination node using the MIGRATE command.
 * 4. Send CLUSTER SETSLOT <slot> NODE <destination-node-id> to the destination node.
 * 5. Send CLUSTER SETSLOT <slot> NODE <destination-node-id> to the source node.
 * 6. Send CLUSTER SETSLOT <slot> NODE <destination-node-id> to the other master nodes (optional).
 *
 *
 * mig [uri] [src-id] [dest-id] [mig] [min] [max]
 *
 * mig redis://10.200.16.65:6879 xxxx yyyy true min max
 */
public class ClusterSlotOnlineMigCmd implements Command {

  @Override
  public void handle(String... args) {
    if (args.length < 2) {
      System.out.println(".. uri pattern");
      return;
    }

    var src = args[1];
    var dst = args[2];

    var mig = Boolean.parseBoolean(args[3]);

    var min = Integer.parseInt(args[4]);
    var max = min;
    if (args.length > 5) {
      max = Integer.parseInt(args[5]);
    }

    if (mig) {
      System.out.println("warning: not supporting mig keys");
    }

    try (var cli = new Jedis(args[0])) {
      var rn = cli.clusterNodes();
      var nodes = rn.split("\n");
      var ms = Arrays.stream(nodes).filter(it -> it.startsWith(src)).findFirst();
      var md = Arrays.stream(nodes).filter(it -> it.startsWith(dst)).findFirst();

      if (ms.isEmpty() || md.isEmpty()) {
        System.out.println("not found src-id or dst-id!");
        return;
      }

      var ss = ms.get().split(" ");
      var sh = ss[1].split("@")[0];

      var ds = md.get().split(" ");
      var dh = ds[1].split("@")[0];

      System.out.println("%s -> %s, begin: %d end: %d, total %d slots".formatted(sh, dh, min, max, max - min + 1));

      // connect src dst
      var s = new Jedis("redis://" + sh);
      var d = new Jedis("redis://" + dh);

      if (!s.isConnected() || !d.isConnected()) {
        System.out.println("src or dst node not connected");
        return;
      }

      try (s; d) {
        for (int i = min; i <= max; i++) {
          System.out.println("do mig " + i);

          d.clusterSetSlotImporting(i, src);
          s.clusterSetSlotMigrating(i, dst);

          // TODO: mig keys

          d.clusterSetSlotNode(i, dst);
          s.clusterSetSlotNode(i, dst);

          System.out.println("done " + i);
        }
      }
    }
  }
}
