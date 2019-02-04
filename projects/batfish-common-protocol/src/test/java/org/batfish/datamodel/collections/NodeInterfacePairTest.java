package org.batfish.datamodel.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class NodeInterfacePairTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new NodeInterfacePair("host", "iface"), new NodeInterfacePair("host", "iface"))
        .addEqualityGroup(new NodeInterfacePair("otherHost", "iface"))
        .addEqualityGroup(new NodeInterfacePair("host", "otherIface"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testCompareTo() {
    List<NodeInterfacePair> ordered =
        ImmutableList.of(
            new NodeInterfacePair("a", "a"),
            new NodeInterfacePair("a", "b"),
            new NodeInterfacePair("b", "a"),
            new NodeInterfacePair("b", "b"));
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
    NodeInterfacePair nip = new NodeInterfacePair("host", "iface");
    assertThat(SerializationUtils.clone(nip), equalTo(nip));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    NodeInterfacePair nip = new NodeInterfacePair("host", "iface");
    assertThat(BatfishObjectMapper.clone(nip, NodeInterfacePair.class), equalTo(nip));
  }
}
