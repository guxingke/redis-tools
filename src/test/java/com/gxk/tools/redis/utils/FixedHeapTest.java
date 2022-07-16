package com.gxk.tools.redis.utils;

import static org.junit.jupiter.api.Assertions.*;

class FixedHeapTest {

  @org.junit.jupiter.api.Test
  void heap_basic() {

    var heap = new FixedHeap<Integer>(100, it -> it);

    for (int i = 0; i < 50; i++) {
      heap.add(i);
    }

    assertEquals(50, heap.size());

    var vr = heap.toSortedList();

    assertEquals(49, vr.get(0));
    assertEquals(0, vr.get(vr.size() - 1));
  }

  @org.junit.jupiter.api.Test
  void heap_basic_2() {

    var heap = new FixedHeap<Integer>(100, it -> it);

    for (int i = 0; i < 150; i++) {
      heap.add(i);
    }

    assertEquals(100, heap.size());

    var vr = heap.toSortedList();

    assertEquals(149, vr.get(0));
    assertEquals(50, vr.get(vr.size() - 1));
  }

  @org.junit.jupiter.api.Test
  void heap_basic_3() {

    var heap = new FixedHeap<Integer>(100, it -> it);

    for (int i = 149; i >= 0; i--) {
      heap.add(i);
    }

    assertEquals(100, heap.size());

    var vr = heap.toSortedList();

    assertEquals(149, vr.get(0));
    assertEquals(50, vr.get(vr.size() - 1));
  }
}