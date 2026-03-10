package org.batfish.common.topology;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link UndirectedEdge}. */
public final class UndirectedEdgeTest {

  @Test
  public void testEquals() {
    UndirectedEdge<Ip> edge1 = new UndirectedEdge<>(Ip.ZERO, Ip.ZERO);
    UndirectedEdge<Ip> edge2 = new UndirectedEdge<>(Ip.ZERO, Ip.FIRST_CLASS_A_PRIVATE_IP);
    UndirectedEdge<Ip> edge3 =
        new UndirectedEdge<>(Ip.FIRST_CLASS_B_PRIVATE_IP, Ip.FIRST_CLASS_A_PRIVATE_IP);

    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(edge1, edge1, new UndirectedEdge<>(Ip.ZERO, Ip.ZERO))
        .addEqualityGroup(edge2)
        .addEqualityGroup(edge3)
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    UndirectedEdge<Ip> edge = new UndirectedEdge<>(Ip.ZERO, Ip.ZERO);

    assertEquals(edge, BatfishObjectMapper.clone(edge, new TypeReference<UndirectedEdge<Ip>>() {}));
  }
}
