package org.batfish.common.topology;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class Layer2NodeTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Layer2Node("h", "i", null), new Layer2Node("h", "i", null))
        .addEqualityGroup(new Layer2Node("h2", "i", null))
        .addEqualityGroup(new Layer2Node("h2", "i2", null))
        .addEqualityGroup(new Layer2Node("h2", "i2", Range.singleton(1)))
        .testEquals();
  }

  @Test
  public void testCanonicalization() {
    assertThat(
        new Layer2Node("h2", "i2", Range.closed(1, 5)),
        equalTo(new Layer2Node("h2", "i2", Range.open(0, 6))));
    assertThat(
        new Layer2Node("h2", "i2", Range.closed(1, 5)),
        equalTo(new Layer2Node("h2", "i2", Range.closedOpen(1, 6))));
    assertThat(
        new Layer2Node("h2", "i2", Range.closed(1, 5)),
        equalTo(new Layer2Node("h2", "i2", Range.openClosed(0, 5))));
  }
}
