package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.TunnelConfiguration.Builder;
import org.junit.Test;

/** Tests of {@link TunnelConfiguration} */
public class TunnelConfigurationTest {
  @Test
  public void testEquals() {
    Builder builder =
        TunnelConfiguration.builder()
            .setDestinationAddress(Ip.parse("1.1.1.1"))
            .setSourceAddress(Ip.parse("2.2.2.2"));
    TunnelConfiguration tc = builder.build();
    new EqualsTester()
        .addEqualityGroup(tc, tc, builder.build())
        .addEqualityGroup(builder.setDestinationAddress(Ip.parse("3.3.3.3")).build())
        .addEqualityGroup(builder.setSourceAddress(Ip.parse("4.4.4.4")))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    TunnelConfiguration tc =
        TunnelConfiguration.builder()
            .setDestinationAddress(Ip.parse("1.1.1.1"))
            .setSourceAddress(Ip.parse("2.2.2.2"))
            .build();
    assertThat(SerializationUtils.clone(tc), equalTo(tc));
  }

  @Test
  public void testJsonSerialization() {
    TunnelConfiguration tc =
        TunnelConfiguration.builder()
            .setDestinationAddress(Ip.parse("1.1.1.1"))
            .setSourceAddress(Ip.parse("2.2.2.2"))
            .build();
    assertThat(BatfishObjectMapper.clone(tc, TunnelConfiguration.class), equalTo(tc));
  }
}
