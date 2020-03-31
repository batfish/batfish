package org.batfish.datamodel.eigrp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.eigrp.EigrpNeighborConfig.Builder;
import org.junit.Test;

public class EigrpNeighborConfigTest {
  @Test
  public void testEquals() {
    Builder builder =
        EigrpNeighborConfig.builder()
            .setAsn(2L)
            .setExportPolicy("policy")
            .setHostname("host")
            .setVrfName("vrf")
            .setInterfaceName("Ethernet0")
            .setIp(Ip.parse("1.1.1.1"));
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setAsn(1L).build())
        .addEqualityGroup(builder.setExportPolicy("policy_new").build())
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
    EigrpNeighborConfig c =
        EigrpNeighborConfig.builder()
            .setAsn(1L)
            .setExportPolicy("policy_new")
            .setHostname("host")
            .setIp(Ip.parse("1.1.1.1"))
            .setVrfName("vrf")
            .setInterfaceName("Ethernet0")
            .build();
    assertThat(SerializationUtils.clone(c), equalTo(c));
  }

  @Test
  public void testJsonSerialization() {
    EigrpNeighborConfig c =
        EigrpNeighborConfig.builder()
            .setAsn(1L)
            .setExportPolicy("policy_new")
            .setHostname("host")
            .setIp(Ip.parse("1.1.1.1"))
            .setVrfName("vrf")
            .setInterfaceName("Ethernet0")
            .build();

    assertThat(BatfishObjectMapper.clone(c, EigrpNeighborConfig.class), equalTo(c));
  }
}
