package com.gxk.tools.redis.cli;

import com.gxk.tools.redis.rdb.Entry;
import com.gxk.tools.redis.rdb.KeyValuePair;
import com.gxk.tools.redis.rdb.RdbParser;
import com.gxk.tools.redis.utils.FixedHeap;
import com.gxk.tools.redis.utils.Tools;
import de.vandermeer.asciitable.AsciiTable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "keyspace", description = "Rdb keyspace overview, based on glob like key pattern aggregation.")
public class RdbKeySpaceCmd implements Runnable {

  @Parameters(index = "0")
  File rdb;

  @Option(names = {"PATTERN", "-p"}, arity = "0..*")
  List<String> patterns;

  @Override
  public void run() {
    boolean up = false;
    List<byte[]> ps = null;
    if (patterns != null) {
      up = true;
      ps = patterns.stream().map(it -> it.getBytes(StandardCharsets.US_ASCII)).toList();
    }

    Map<String, Integer> keyspace = new HashMap<>();
    int total = 0;
    try (var p = new RdbParser(rdb)) {
      Entry e;
      while ((e = p.readNext()) != null) {
        switch (e.getType()) {
          case KEY_VALUE_PAIR -> {
            var pair = (KeyValuePair) e;
            var key = pair.getKey();
            total++;

            // try ps
            boolean upm = false;
            if (up) {
              for (byte[] pattern : ps) {
                if (Tools.match(key, pattern)) {
                  upm = true;
                  keyspace.compute(new String(pattern), (key1, old) -> {
                    if (old == null) {
                      return 1;
                    }
                    return old + 1;
                  });
                }
              }
            }
            if (upm) {
              continue;
            }

            // predict
            var t = normalize2(key);
            keyspace.compute(t, (key1, old) -> {
              if (old == null) {
                return 1;
              }
              return old + 1;
            });
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    var heap = new FixedHeap<Cnt>(100, it -> it.total);

    for (Map.Entry<String, Integer> e : keyspace.entrySet()) {
      heap.add(new Cnt(e.getKey(), e.getValue()));
    }

    System.out.println("total: " + total);
    System.out.println();

    var t = new AsciiTable();

    t.addRule();
    t.addRow("keyspace", "total");
    t.addRule();

    var sh = heap.toSortedList();
    for (Cnt cnt : sh) {
      if (cnt.total <= 1) {
        continue;
      }
      t.addRow(cnt.keyspace, cnt.total);
      t.setPaddingLeft(1);
    }
    t.addRule();

    System.out.println(t.render(100));
  }

  static class Cnt {

    final String keyspace;
    final int total;

    Cnt(
        String keyspace,
        int total
    ) {
      this.keyspace = keyspace;
      this.total = total;
    }
  }

  public static String normalize2(
      byte[] key
  ) {
    // 分隔符： : . | - _ , " '
    var len = key.length;

    var f = 0;
    var idx = 0;
    while (idx < len) {
      while (!isDelimiter(key[idx])) {
        idx++;
        if (idx == len) {
          break;
        }
      }
      // delimiter or end
      if (isIdentifier(key, f, idx)) {
        if (idx - f > 3) { // ..
          for (int i = f; i < idx; i++) {
            key[i] = '0';
          }
        }
      }
      idx++;
      f = idx;
    }

    var v = new String(key);
    return v.replaceAll("0{3,}", "*");
  }

  private static boolean isDelimiter(byte b) {
    return b == ':' || b == '.' || b == '|' || b == '_' || b == ',' || b == ' ' || b == '\'' || b == '\"';
  }

  private static boolean isIdentifier(
      byte[] bytes,
      int begin,
      int end
  ) {
    // is number
    if (isNumber(bytes, begin, end)) {
      return true;
    }

    // is hex number
    return isHexNumber(bytes, begin, end);
  }

  private static boolean isHexNumber(
      byte[] bytes,
      int begin,
      int end
  ) {
    for (int i = begin; i < end; i++) {
      var b = bytes[i];

      var n = b >= '0' && b <= '9';
      var a = b >= 'A' && b <= 'F';
      var c = b >= 'a' && b <= 'f';

      if (!a && !c && !n && b != '-') {
        return false;
      }
    }
    return true;
  }

  private static boolean isNumber(
      byte[] bytes,
      int begin,
      int end
  ) {
    for (int i = begin; i < end; i++) {
      var b = bytes[i];

      if (b < '0' || b > '9') {
        return false;
      }
    }
    return true;
  }
}
