package com.gxk.tools.redis.cli;

import com.gxk.tools.redis.rdb.AuxField;
import com.gxk.tools.redis.rdb.Entry;
import com.gxk.tools.redis.rdb.KeyValuePair;
import com.gxk.tools.redis.rdb.RdbParser;
import com.gxk.tools.redis.rdb.ValueType;
import de.vandermeer.asciitable.AsciiTable;
import java.io.File;
import java.util.List;
import java.util.Objects;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

/**
 * rdb 内存概要
 */
@CommandLine.Command(name = "mem", description = "Rdb memory overview, memory used, ttl etc.")
public class RdbMemoryCmd implements Runnable{

  @Parameters(index = "0")
  File rdb;

  // TTL 统计,
  // never expire, 0-1min, 1-5min, 5-30min, 30-60min, 1-12hr, 12-24hr, 24hr+
  private int[] ttl = new int[8];
  // memory by DATA TYPE
  private int[] mtk = new int[6];
  private int[] mtb = new int[6];

  private String[] st = new String[6];
  // num keys by data type
  private int[] nt = new int[6];

  @Override
  public void run() {
    long ctime = System.currentTimeMillis();

    int total = 0;
    byte[] key = null;
    try (var p = new RdbParser(rdb)) {
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
            key = pair.getKey();
            total++;

            var vt = pair.getValueType();
            var pos = pos(vt);
            nt[pos]++;
            mtb[pos] += valueSize(pair.getValues());

            if (mtb[pos] > 1024 * 1000) { // > 1k
              var t = mtb[pos] / 1024;
              mtk[pos] += t;
              mtb[pos] -= t * 1024;
            }
            if (st[pos] == null) { // first key
              st[pos] = new String(key);
            }

            // ttl
            var et = pair.getExpireTime();
            ttl[pos(et, ctime)]++;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(new String(Objects.requireNonNull(key)));
    }

    var t = new AsciiTable();
    // sum
    t.addRule();
    t.addRow("Type", "Memory", "Number of Keys", "Top Key");
    t.addRule();
    t.addRow("Total", "0", total + "", "");
    t.setPaddingLeft(1);
    t.addRule();

    if (nt[0] > 0) {
      // value
      t.addRow("Value", format(mtk[0], mtb[0]), "" + nt[0], st[0]);
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    if (nt[1] > 0) {
      // List
      t.addRow("List", format(mtk[1], mtb[1]), "" + nt[1], st[1]);
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    if (nt[2] > 0) {
      // Set
      t.addRow("Set", format(mtk[2], mtb[2]), "" + nt[2], st[2]);
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }
    // Zset
    if (nt[3] > 0) {
      t.addRow("Zset", format(mtk[3], mtb[3]), "" + nt[3], st[3]);
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }
    if (nt[4] > 0) {
      // Hash
      t.addRow("Hash", format(mtk[4], mtb[4]), "" + nt[4], st[4]);
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    System.out.println("Memory Overview");
    System.out.println(t.render(100));

    // ttl

    t = new AsciiTable();
    // sum
    t.addRule();
    t.addRow("Range", "Number of Keys", "Percentage");
    t.addRule();
    t.addRow("Total", total + "", "");
    t.setPaddingLeft(1);
    t.addRule();

    if (ttl[0] > 0) {
      t.addRow("Never Expire", ttl[0], percent(ttl[0], total));
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    if (ttl[1] > 0) {
      t.addRow("0-1 Min", ttl[1], percent(ttl[1], total));
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    if (ttl[2] > 0) {
      t.addRow("1-5 Min", ttl[2], percent(ttl[2], total));
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    if (ttl[3] > 0) {
      t.addRow("5-30 Min", ttl[3], percent(ttl[3], total));
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    if (ttl[4] > 0) {
      t.addRow("30-60 Min", ttl[4], percent(ttl[4], total));
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    if (ttl[5] > 0) {
      t.addRow("1-12 Hr", ttl[5], percent(ttl[5], total));
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    if (ttl[6] > 0) {
      t.addRow("12-24 Hr", ttl[6], percent(ttl[6], total));
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    if (ttl[7] > 0) {
      t.addRow("24+ Hr", ttl[7], percent(ttl[7], total));
      t.setPaddingLeft(1);
      t.setPaddingRight(1);
      t.addRule();
    }

    System.out.println("TTL Overview");
    System.out.println(t.render(100));
  }

  private Object format(
      int kbs,
      int bytes
  ) {
    if (kbs == 0) {
      return bytes + " Byte";
    }
    if (kbs > 1024 * 1024) { // G
      return String.format("%.2f GB", kbs / 1024 / 1024.0);
    }
    if (kbs > 1024) { // Mb
      return String.format("%.2f MB", kbs / 1024.0);
    }
    return kbs + " KB";
  }

  private int pos(ValueType type) {
    if (type.ordinal() < 5) {
      return type.ordinal();
    }
    return switch (type) {
      case VALUE -> 0;
      case LIST -> 1;
      case SET -> 2;
      case SORTED_SET -> 3;
      case HASH -> 4;
      case ZIPMAP -> 4;
      case ZIPLIST -> 1;
      case INTSET -> 2;
      case SORTED_SET_AS_ZIPLIST -> 3;
      case HASHMAP_AS_ZIPLIST -> 4;
      case QUICKLIST -> 1;
      case SORTED_SET2 -> 3;
    };
  }

  private int pos(
      Long et,
      long ctime
  ) {
    if (et == null || et < 0L) {
      return 0; // never expire
    }
    et = et - ctime;
    if (et < 60 * 1000) { // 1min
      return 1;
    }
    if (et < 5 * 60 * 1000) { // 5 min
      return 2;
    }

    if (et < 30 * 60 * 1000) { // 30 min
      return 3;
    }

    if (et < 60 * 60 * 1000) { // 60 min
      return 4;
    }

    if (et < 12 * 60 * 60 * 1000) { // 12 hr
      return 5;
    }

    if (et < 24 * 60 * 60 * 1000) { // 24 hr
      return 6;
    }

    return 7; // 24 hr +
  }

  private int valueSize(List<byte[]> values) {
    if (values == null || values.isEmpty()) {
      return 0;
    }
    return values.stream().map(it -> it.length).mapToInt(Integer::intValue).sum();
  }

  private String percent(
      int n,
      int total
  ) {
    if (n == total) {
      return "####################"; // # for 20 times
    }

    var v = n * 100.0 / total / 5;
    if (v > 20) {
      v = 20;
    }
    return "#".repeat(((int) (Math.round(v))));
  }
}
