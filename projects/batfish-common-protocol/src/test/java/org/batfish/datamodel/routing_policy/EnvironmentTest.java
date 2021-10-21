package org.batfish.datamodel.routing_policy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.junit.Test;

public final class EnvironmentTest {
  @Test
  public void testNoBgpProperties() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.UNKNOWN)
            .build();
    // No BGP properties
    Environment e = Environment.builder(c).build();
    assertThat(e.getLocalAs(), equalTo(Optional.empty()));
    assertThat(e.getLocalIp(), equalTo(Optional.empty()));
    assertThat(e.getRemoteAs(), equalTo(Optional.empty()));
    assertThat(e.getRemoteIp(), equalTo(Optional.empty()));
  }

  @Test
  public void testBgpProperties() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.UNKNOWN)
            .build();
    BgpSessionProperties props =
        BgpSessionProperties.builder()
            .setHeadAs(1)
            .setTailAs(2)
            .setHeadIp(Ip.ZERO)
            .setTailIp(Ip.MAX)
            .build();
    Environment eIn =
        Environment.builder(c).setBgpSessionProperties(props).setDirection(Direction.IN).build();
    assertThat(eIn.getLocalAs(), equalTo(Optional.of(1L)));
    assertThat(eIn.getLocalIp(), equalTo(Optional.of(Ip.ZERO)));
    assertThat(eIn.getRemoteAs(), equalTo(Optional.of(2L)));
    assertThat(eIn.getRemoteIp(), equalTo(Optional.of(Ip.MAX)));
    Environment eOut =
        Environment.builder(c).setBgpSessionProperties(props).setDirection(Direction.OUT).build();
    assertThat(eOut.getLocalAs(), equalTo(Optional.of(2L)));
    assertThat(eOut.getLocalIp(), equalTo(Optional.of(Ip.MAX)));
    assertThat(eOut.getRemoteAs(), equalTo(Optional.of(1L)));
    assertThat(eOut.getRemoteIp(), equalTo(Optional.of(Ip.ZERO)));
  }
}
