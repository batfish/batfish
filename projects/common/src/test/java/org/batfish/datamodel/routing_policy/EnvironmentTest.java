package org.batfish.datamodel.routing_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
            .setRemoteAs(1)
            .setLocalAs(2)
            .setRemoteIp(Ip.ZERO)
            .setLocalIp(Ip.MAX)
            .build();
    Environment environment =
        Environment.builder(c).setBgpSessionProperties(props).setDirection(Direction.IN).build();
    assertThat(environment.getLocalAs(), equalTo(Optional.of(2L)));
    assertThat(environment.getLocalIp(), equalTo(Optional.of(Ip.MAX)));
    assertThat(environment.getRemoteAs(), equalTo(Optional.of(1L)));
    assertThat(environment.getRemoteIp(), equalTo(Optional.of(Ip.ZERO)));
  }
}
