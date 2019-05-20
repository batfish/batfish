package org.batfish.datamodel.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests of {@link IpEdge}. */
public class IpEdgeTest {
  @Test
  public void testJavaSerialization() {
    IpEdge e = new IpEdge("A", Ip.FIRST_CLASS_A_PRIVATE_IP, "B", Ip.FIRST_CLASS_E_EXPERIMENTAL_IP);
    assertThat(SerializationUtils.clone(e), equalTo(e));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    IpEdge e = new IpEdge("A", Ip.FIRST_CLASS_A_PRIVATE_IP, "B", Ip.FIRST_CLASS_E_EXPERIMENTAL_IP);
    assertThat(BatfishObjectMapper.clone(e, IpEdge.class), equalTo(e));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IpEdge("a", Ip.FIRST_CLASS_A_PRIVATE_IP, "b", Ip.FIRST_CLASS_B_PRIVATE_IP),
            new IpEdge("a", Ip.FIRST_CLASS_A_PRIVATE_IP, "b", Ip.FIRST_CLASS_B_PRIVATE_IP))
        .addEqualityGroup(
            new IpEdge("a", Ip.FIRST_CLASS_A_PRIVATE_IP, "b", Ip.FIRST_CLASS_C_PRIVATE_IP))
        .addEqualityGroup(
            new IpEdge("a", Ip.FIRST_CLASS_C_PRIVATE_IP, "c", Ip.FIRST_CLASS_B_PRIVATE_IP))
        .addEqualityGroup(
            new IpEdge("b", Ip.FIRST_CLASS_A_PRIVATE_IP, "b", Ip.FIRST_CLASS_B_PRIVATE_IP))
        .testEquals();
  }

  @Test
  public void testCompareTo() {
    List<IpEdge> edges =
        ImmutableList.of(
            new IpEdge("a", Ip.ZERO, "b", Ip.ZERO),
            new IpEdge("a", Ip.ZERO, "b", Ip.MAX),
            new IpEdge("a", Ip.ZERO, "c", Ip.MAX),
            new IpEdge("a", Ip.MAX, "c", Ip.MAX),
            new IpEdge("b", Ip.MAX, "c", Ip.MAX));

    assertThat(Lists.reverse(edges).stream().sorted().collect(Collectors.toList()), equalTo(edges));
  }
}
