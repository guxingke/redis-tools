package com.gxk.tools.redis.utils;

import de.vandermeer.asciitable.AsciiTable;
import org.junit.jupiter.api.Test;

public class TableTest {

  @Test
  public void test_span() {
    AsciiTable at = new AsciiTable();
    at.addRule();
    at.addRow(null, null, null, null, "span all 5 columns");
    at.addRule();
    at.addRow(null, null, null, "span 4 columns", "just 1 column");
    at.addRule();
    at.addRow(null, null, "span 3 columns", null, "span 2 columns");
    at.addRule();
    at.addRow(null, "span 2 columns", null, null, "span 3 columns");
    at.addRule();
    at.addRow("just 1 column", null, null, null, "span 4 columns");
    at.addRule();
    at.addRow("just 1 column", "just 1 column", "just 1 column", "just 1 column", "just 1 column");
    at.addRule();
    System.out.println(at.render());
  }
}
