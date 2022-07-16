package com.gxk.tools.redis.utils;

import com.gxk.tools.redis.rdb.ValueType;

public interface Tools {

  /**
   * match like redis SCAN MATCH
   *
   */
  static boolean match(
      byte[] key,
      byte[] pattern
  ) {
    // pattern contains *
    boolean glob = false;
    for (byte b : pattern) {
      if (b == '*') {
        glob = true;
        break;
      }
    }
    if (!glob) {
      // 全匹配
      if (key.length != pattern.length) {
        return false;
      }

      for (int i = 0; i < key.length; i++) {
        if (key[i] != pattern[i]) {
          return false;
        }
      }
    }
    // glob

    byte mb = -1;
    int ki = 0;
    int pi = 0;
    var pl = pattern.length;
    var kl = key.length;

    while (pi < pl) {
      var cp = pattern[pi];

      if (cp == '*') { // 模糊匹配
        // 找下一个不是 * 的字符
        while (pi + 1 < pl && pattern[++pi] != '*') {
          // found
          mb = pattern[pi];
          break;
        }

        if (mb == -1) { // * 为最后一个, 或者说末尾都是 *
          return true;
        } else {
          // skip current ki
          while (ki + 1 < kl && key[++ki] != mb) {
          }
          if (ki == kl) {
            // not found
            return false;
          }

          mb = -1;
          pi++;
          ki++;

          if (ki == kl) { // last one
            if (pi != pl) {
              return false;
            }
            return true;
          }
        }
      } else {
        if (key[ki] != cp) {
          return false;
        }
        pi++;
        ki++;

        if (ki == kl) { // last one
          return true;
        }
      }

    }
    return true;
  }

  static boolean typeMatch(
      ValueType type,
      String u
  ) {
    boolean v = false;
    switch (type) {
      case VALUE -> v = u.equalsIgnoreCase("String");
      case SET, INTSET -> v = u.equalsIgnoreCase("SET");
      case HASH, ZIPMAP, HASHMAP_AS_ZIPLIST -> v = u.equalsIgnoreCase("Hash");
      case SORTED_SET, SORTED_SET2, SORTED_SET_AS_ZIPLIST -> v = u.equalsIgnoreCase("Zset");
      case LIST, ZIPLIST, QUICKLIST -> v = u.equalsIgnoreCase("List");
      default -> v = false;
    }
    return v;
  }
}
