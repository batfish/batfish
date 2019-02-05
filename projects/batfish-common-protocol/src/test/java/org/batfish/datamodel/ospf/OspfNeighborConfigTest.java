package org.batfish.datamodel.ospf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
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
            .setInterfaceName("Ethernet0");
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setArea(2L).build())
        .addEqualityGroup(builder.setInterfaceName("Ethernet11").build())
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
            .setVrfName("vrf")
            .setInterfaceName("Ethernet0")
            .build();
    assertThat(SerializationUtils.clone(c), equalTo(c));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    OspfNeighborConfig c =
        OspfNeighborConfig.builder()
            .setArea(1L)
            .setHostname("host")
            .setVrfName("vrf")
            .setInterfaceName("Ethernet0")
            .build();

    assertThat(BatfishObjectMapper.clone(c, OspfNeighborConfig.class), equalTo(c));
  }
}
