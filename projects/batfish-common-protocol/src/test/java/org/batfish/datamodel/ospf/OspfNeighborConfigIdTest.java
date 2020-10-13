package org.batfish.datamodel.ospf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.junit.Test;

/** Tests of {@link OspfNeighborConfigId} */
public class OspfNeighborConfigIdTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new OspfNeighborConfigId(
                "h", "v", "p", "i", ConcreteInterfaceAddress.parse("192.0.2.0/31")),
            new OspfNeighborConfigId(
                "h", "v", "p", "i", ConcreteInterfaceAddress.parse("192.0.2.0/31")))
        .addEqualityGroup(
            new OspfNeighborConfigId(
                "h2", "v", "p", "i", ConcreteInterfaceAddress.parse("192.0.2.0/31")))
        .addEqualityGroup(
            new OspfNeighborConfigId(
                "h", "v2", "p", "i", ConcreteInterfaceAddress.parse("192.0.2.0/31")))
        .addEqualityGroup(
            new OspfNeighborConfigId(
                "h", "v", "p2", "i", ConcreteInterfaceAddress.parse("192.0.2.0/31")))
        .addEqualityGroup(
            new OspfNeighborConfigId(
                "h", "v", "p", "i2", ConcreteInterfaceAddress.parse("192.0.2.0/31")))
        .addEqualityGroup(
            new OspfNeighborConfigId(
                "h", "v", "p", "i2", ConcreteInterfaceAddress.parse("192.0.2.2/31")))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    OspfNeighborConfigId cid =
        new OspfNeighborConfigId(
            "h", "v", "p", "i", ConcreteInterfaceAddress.parse("192.0.2.2/31"));
    assertThat(SerializationUtils.clone(cid), equalTo(cid));
  }

  @Test
  public void testJsonSerialization() {
    OspfNeighborConfigId cid =
        new OspfNeighborConfigId(
            "h", "v", "p", "i", ConcreteInterfaceAddress.parse("192.0.2.2/31"));
    assertThat(BatfishObjectMapper.clone(cid, OspfNeighborConfigId.class), equalTo(cid));
  }
}
