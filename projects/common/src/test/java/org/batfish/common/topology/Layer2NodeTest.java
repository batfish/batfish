package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Range;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
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

  @Test
  public void testSerialization() {
    Layer2Node original = new Layer2Node("h", "i", Range.openClosed(0, 3));
    assertThat(original, equalTo(BatfishObjectMapper.clone(original, Layer2Node.class)));
    assertThat(original, equalTo(SerializationUtils.clone(original)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRejectEmpty() {
    new Layer2Node("h", "i", Range.open(0, 1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRejectEmpty2() {
    new Layer2Node("h", "i", Range.closedOpen(0, 0));
  }
}
