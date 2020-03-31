package org.batfish.datamodel.ospf;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.ospf.OspfNeighborConfig.Builder;
import org.junit.Test;

/** Tests of {@link OspfNeighborConfig} */
public class OspfNeighborConfigTest {
  @Test
  public void testEquals() {
    Builder builder =
        OspfNeighborConfig.builder()
            .setArea(1L)
            .setHostname("host")
            .setVrfName("vrf")
            .setInterfaceName("Ethernet0")
            .setIp(Ip.parse("1.1.1.1"));
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setArea(2L).build())
        .addEqualityGroup(builder.setInterfaceName("Ethernet11").build())
        .addEqualityGroup(builder.setIp(Ip.parse("2.2.2.2")))
        .addEqualityGroup(builder.setPassive(true).build())
        .addEqualityGroup(builder.setHostname("otherHost").build())
        .addEqualityGroup(builder.setVrfName("otherVRF").build())
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    OspfNeighborConfig c =
        OspfNeighborConfig.builder()
            .setArea(1L)
            .setHostname("host")
            .setIp(Ip.parse("1.1.1.1"))
            .setVrfName("vrf")
            .setInterfaceName("Ethernet0")
            .build();
    assertThat(SerializationUtils.clone(c), equalTo(c));
  }

  @Test
  public void testJsonSerialization() {
    OspfNeighborConfig c =
        OspfNeighborConfig.builder()
            .setArea(1L)
            .setHostname("host")
            .setIp(Ip.parse("1.1.1.1"))
            .setVrfName("vrf")
            .setInterfaceName("Ethernet0")
            .build();

    assertThat(BatfishObjectMapper.clone(c, OspfNeighborConfig.class), equalTo(c));
  }
}
