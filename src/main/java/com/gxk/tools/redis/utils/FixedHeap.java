package com.gxk.tools.redis.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * 固定大小的堆，用来存储最大的 k 个元素
 */
public class FixedHeap<T> {

  private final int size;
  private final Object[] values;
  private final int[] vv;

  /**
   * 用来获取权重
   */
  private final Function<T, Integer> fun;

  int cur;
  int max;
  int min;

  public FixedHeap(int maxSize, Function<T, Integer> fun) {
    this.size = maxSize;
    this.fun = fun;
    this.values = new Object[this.size];
    this.vv = new int[this.size];
    Arrays.fill(this.vv, Integer.MAX_VALUE);
    this.min = Integer.MAX_VALUE;
  }


  public void add(T e) {
    var v = fun.apply(e);

    if (cur < size) { // 还没满
      values[cur] = e;
      vv[cur++] = v;

      min = Math.min(min, v);
      max = Math.max(max, v);
      return;
    }

    if (v <= min) { // do nothings
      return;
    }

    if (v >= max) { // 进
      // 找到 min 对应的 index

      int i;
      for (i = 0; i < vv.length; i++) {
        if (vv[i] == min) { // found
          break;
        }
      }

      if (i == vv.length) { // not found
        // bad case
        throw new IllegalStateException();
      }
      values[i] = e;
      vv[i] = v;

      // calc min
      min = Integer.MAX_VALUE;
      for (int j = 0; j < cur; j++) {
        min = Math.min(min, vv[j]);
      }
      // next
    }
  }

  // real usage size
  public int size() {
    return cur;
  }

  public int capacity() {
    return size;
  }

  public List<T> toSortedList() {
    // sort
    var vs = new ArrayList<T>(size);
    for (Object v : this.values) {
      if (v != null) {
        vs.add((T) v);
      }
    }

    vs.sort((o1, o2) -> fun.apply(o2) - fun.apply(o1));
    return vs;
  }
}
