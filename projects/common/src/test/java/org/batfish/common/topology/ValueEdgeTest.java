package org.batfish.common.topology;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link ValueEdge}. */
public final class ValueEdgeTest {

  @Test
  public void testEquals() {
    ValueEdge<Ip, String> edge1 = new ValueEdge<>(Ip.ZERO, Ip.ZERO, "a");
    ValueEdge<Ip, String> edge2 = new ValueEdge<>(Ip.ZERO, Ip.ZERO, "b");
    ValueEdge<Ip, String> edge3 = new ValueEdge<>(Ip.ZERO, Ip.FIRST_CLASS_A_PRIVATE_IP, "b");
    ValueEdge<Ip, String> edge4 =
        new ValueEdge<>(Ip.FIRST_CLASS_B_PRIVATE_IP, Ip.FIRST_CLASS_A_PRIVATE_IP, "b");

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(edge1, edge1, new ValueEdge<>(Ip.ZERO, Ip.ZERO, "a"))
        .addEqualityGroup(edge2)
        .addEqualityGroup(edge3)
        .addEqualityGroup(edge4)
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    ValueEdge<Ip, String> edge = new ValueEdge<>(Ip.ZERO, Ip.ZERO, "a");

    assertEquals(
        edge, BatfishObjectMapper.clone(edge, new TypeReference<ValueEdge<Ip, String>>() {}));
  }
}
