package com.gxk.tools.redis;

import com.gxk.tools.redis.core.Registry;
import com.gxk.tools.redis.script.ClusterSlotMigCmd;
import com.gxk.tools.redis.script.ClusterSlotOnlineMigCmd;
import com.gxk.tools.redis.script.ClusterStatusCmd;
import com.gxk.tools.redis.script.ScanAndDelCommand;
import com.gxk.tools.redis.script.ScanAndHdelCommand;
import com.gxk.tools.redis.script.ScanAndHgetCommand;
import com.gxk.tools.redis.script.ScanCommand;
import com.gxk.tools.redis.script.TestCommand;
import java.util.Arrays;

/**
 * 旧的启动入口
 *
 * @see CliMain
 */
@Deprecated
public class Main {

  public static void main(String[] args) {

    if (args.length < 1) {
      System.out.println("!!! ...");
      return;
    }

    init();

    final var ns = args[0];
    final var handler = Registry.lookup(ns);
    if (handler == null) {
      // bad case
      System.exit(-1);
      return;
    }

    final var innerArgs = new String[args.length - 1];
    System.arraycopy(args, 1, innerArgs, 0, innerArgs.length);

    final var begin = System.currentTimeMillis();
    System.out.println("%d found handler and prepared args, %s %s".formatted(begin,
                                                                             handler.getClass().getSimpleName(),
                                                                             Arrays.toString(innerArgs)));
    System.out.println("================================================================================");
    System.out.println();

    handler.handle(innerArgs);

    final var end = System.currentTimeMillis();

    System.out.println();
    System.out.println("================================================================================");
    System.out.println("%d end, cost %d s".formatted(end, (end - begin) / 1000));
  }

  public static void init() {
    Registry.register("test", TestCommand.class);
    Registry.register("scan", ScanCommand.class);
    Registry.register("scan-hdel", ScanAndHdelCommand.class);
    Registry.register("scan-hget", ScanAndHgetCommand.class);
    Registry.register("scan-del", ScanAndDelCommand.class);

    // cluster
    Registry.register("cluster-offline-mig", ClusterSlotMigCmd.class);
    Registry.register("cluster-online-mig", ClusterSlotOnlineMigCmd.class);
    Registry.register("status", ClusterStatusCmd.class);
  }
}
