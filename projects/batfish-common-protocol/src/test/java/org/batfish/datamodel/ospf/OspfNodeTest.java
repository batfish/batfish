package org.batfish.datamodel.ospf;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link OspfNode}. */
public class OspfNodeTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new OspfNode("a", "i", new Ip("1.2.3.4")), new OspfNode("a", "i", new Ip("1.2.3.4")))
        .addEqualityGroup(new OspfNode("a", "i", new Ip("1.2.3.5")))
        .addEqualityGroup(new OspfNode("b", "i", new Ip("1.2.3.4")))
        .addEqualityGroup(new OspfNode("a", "j", new Ip("1.2.3.4")))
        .testEquals();
  }

  @Test
  public void testComparison() {
    List<OspfNode> expected =
        ImmutableList.of(
            new OspfNode("a", "i", new Ip("1.2.3.4")),
            new OspfNode("a", "i", new Ip("1.2.3.5")),
            new OspfNode("a", "j", new Ip("1.2.3.4")),
            new OspfNode("a", "j", new Ip("1.2.3.5")),
            new OspfNode("b", "i", new Ip("1.2.3.4")),
            new OspfNode("b", "i", new Ip("1.2.3.5")));
    assertThat(Ordering.natural().sortedCopy(expected), equalTo(expected));
    assertThat(Ordering.natural().sortedCopy(Lists.reverse(expected)), equalTo(expected));
  }

  @Test
  public void testSerialization() throws IOException {
    OspfNode node = new OspfNode("a", "i", new Ip("1.2.3.4"));
    assertThat(BatfishObjectMapper.clone(node, OspfNode.class), equalTo(node));
  }
}
