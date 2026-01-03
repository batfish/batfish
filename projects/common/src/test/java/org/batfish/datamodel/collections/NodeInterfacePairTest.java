package org.batfish.datamodel.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class NodeInterfacePairTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            NodeInterfacePair.of("host", "iface"),
            NodeInterfacePair.of("host", "iface"),
            NodeInterfacePair.of("HOST", "iface"))
        .addEqualityGroup(NodeInterfacePair.of("otherHost", "iface"))
        .addEqualityGroup(NodeInterfacePair.of("host", "otherIface"))
        .addEqualityGroup(NodeInterfacePair.of("host", "IFACE"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testCompareTo() {
    List<NodeInterfacePair> ordered =
        ImmutableList.of(
            NodeInterfacePair.of("a", "a1"),
            NodeInterfacePair.of("a", "a2"),
            NodeInterfacePair.of("a", "a10"),
            NodeInterfacePair.of("a", "b"),
            NodeInterfacePair.of("b", "a"),
            NodeInterfacePair.of("b", "b"));
    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(ordered.get(i).compareTo(ordered.get(j))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }

  @Test
  public void testJavaSerialization() {
    NodeInterfacePair nip = NodeInterfacePair.of("host", "iface");
    assertThat(SerializationUtils.clone(nip), equalTo(nip));
  }

  @Test
  public void testJsonSerialization() {
    NodeInterfacePair nip = NodeInterfacePair.of("host", "iface");
    assertThat(BatfishObjectMapper.clone(nip, NodeInterfacePair.class), equalTo(nip));
  }
}
