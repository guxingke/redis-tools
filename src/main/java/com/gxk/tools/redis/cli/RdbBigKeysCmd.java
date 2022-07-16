package com.gxk.tools.redis.cli;

import com.gxk.tools.redis.rdb.AuxField;
import com.gxk.tools.redis.rdb.Entry;
import com.gxk.tools.redis.rdb.KeyValuePair;
import com.gxk.tools.redis.rdb.RdbParser;
import com.gxk.tools.redis.utils.FixedHeap;
import de.vandermeer.asciitable.AsciiTable;
import java.io.File;
import java.util.List;
import java.util.Objects;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * bigkeys [rdb] TYPE type COUNT count
 */
@CommandLine.Command(name = "bigkeys", description = "Search rdb big keys, sort by memory used.")
public class RdbBigKeysCmd implements Runnable {

  @Parameters(index = "0")
  File file;

  @Option(names = {"COUNT", "-c"}, defaultValue = "20")
  int count;

  @Option(names = {"TYPE", "-t"}, defaultValue = "String Hash List Set ZSet", split = " ")
  List<String> types;

  @Override
  public void run() {
    var lq = new FixedHeap<Pair>(count, it -> it.mem);
    var hq = new FixedHeap<Pair>(count, it -> it.mem);
    var sq = new FixedHeap<Pair>(count, it -> it.mem);
    var zq = new FixedHeap<Pair>(count, it -> it.mem);
    var vq = new FixedHeap<Pair>(count, it -> it.mem);

    int total = 0;
    long ctime = System.currentTimeMillis();
    try (var p = new RdbParser(file)) {
      Entry e;
      while ((e = p.readNext()) != null) {
        switch (e.getType()) {
          case AUX_FIELD -> {
            var af = (AuxField) e;
            var ak = new String(af.getKey());
            if (Objects.equals(ak, "ctime")) {
              ctime = Long.parseLong(new String(af.getValue())) * 1000;
            }
          }
          case KEY_VALUE_PAIR -> {
            var pair = (KeyValuePair) e;
            var key = pair.getKey();
            total++;

            switch (pair.getValueType()) {
              case VALUE -> {
                vq.add(new Pair(new String(key),
                                size(pair.getValues()),
                                pair.getValues().size(),
                                pair.getExpireTime()));
              }
              case HASH, HASHMAP_AS_ZIPLIST, ZIPMAP -> {
                hq.add(new Pair(new String(key),
                                size(pair.getValues()),
                                pair.getValues().size(),
                                pair.getExpireTime()));
              }
              case LIST, QUICKLIST, ZIPLIST -> {
                lq.add(new Pair(new String(key),
                                size(pair.getValues()),
                                pair.getValues().size(),
                                pair.getExpireTime()));
              }
              case SET, INTSET -> {
                sq.add(new Pair(new String(key),
                                size(pair.getValues()),
                                pair.getValues().size(),
                                pair.getExpireTime()));
              }
              case SORTED_SET, SORTED_SET_AS_ZIPLIST, SORTED_SET2 -> {
                zq.add(new Pair(new String(key),
                                size(pair.getValues()),
                                pair.getValues().size(),
                                pair.getExpireTime()));
              }
              default -> {
                // nothings
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("total: %d".formatted(total));

    System.out.println();

    // top values
    if (vq.size() > 0) {
      System.out.println("Biggest String Keys");
      System.out.println(table("String", vq, ctime).render(100));
    }

    // top dict
    if (hq.size() > 0) {
      System.out.println("Biggest Hash Keys");
      System.out.println(table("Hash", hq, ctime).render(100));
    }

    // top list
    if (lq.size() > 0) {
      System.out.println("Biggest List Keys");
      System.out.println(table("List", lq, ctime).render(100));
    }

    // top set
    if (sq.size() > 0) {
      System.out.println("Biggest Set Keys");
      System.out.println(table("Set", sq, ctime).render(100));
    }

    // top zset
    if (zq.size() > 0) {
      System.out.println("Biggest Zset Keys");
      System.out.println(table("Zset", zq, ctime).render(100));
    }
  }

  private AsciiTable table(
      String type,
      FixedHeap<Pair> heap,
      long ctime
  ) {
    var t = new AsciiTable();

    t.addRule();
    t.addRow("key", "type", "mem(byte)", "size", "ttl(ms)");
    t.addRule();

    var hp = heap.toSortedList();
    for (Pair p : hp) {
      t.addRow(p.key, type, p.mem, p.size, p.ttl == null ? -1L : p.ttl - ctime);
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
    }
    t.addRule();
    return t;
  }

  record Pair(String key, int mem, int size, Long ttl) {

  }

  static int size(List<byte[]> vals) {
    return vals.stream().map(it -> it.length).mapToInt(Integer::intValue).sum();
  }
}
