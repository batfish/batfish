package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.junit.Test;

/** Tests of {@link NetworkConfigurations} */
public class NetworkConfigurationsTest {

  @Test
  public void testWrapConfigs() {
    NetworkConfigurations nc = NetworkConfigurations.of(ImmutableMap.of());
    assertThat(nc.getMap(), anEmptyMap());
    assertThat(nc.get("foo"), equalTo(Optional.empty()));
  }

  @Test
  public void testGetVrf() {
    Configuration c = new Configuration("foo", ConfigurationFormat.CISCO_IOS);
    c.getVrfs().put("fooVRF", new Vrf("fooVRF"));
    NetworkConfigurations nc = NetworkConfigurations.of(ImmutableMap.of("foo", c));

    // Missing values are null
    assertThat(nc.getVrf("missingHostname", "missingVRF"), equalTo(Optional.empty()));
    assertThat(nc.getVrf("missingHostname", "fooVRF"), equalTo(Optional.empty()));
    assertThat(nc.getVrf("foo", "missingVRF"), equalTo(Optional.empty()));

    // Actual get succeeds
    assertTrue("VRF is present", nc.getVrf("foo", "fooVRF").isPresent());
  }

  @Test
  public void testGetInterface() {
    Configuration c = new Configuration("foo", ConfigurationFormat.CISCO_IOS);
    Interface i = TestInterface.builder().setBandwidth(1e9).setName("eth0").build();
    c.getAllInterfaces().put(i.getName(), i);

    NetworkConfigurations nc = NetworkConfigurations.of(ImmutableMap.of("foo", c));

    assertThat(nc.getInterface("bar", "missingIface"), equalTo(Optional.empty()));
    assertThat(nc.getInterface("foo", "missingIface"), equalTo(Optional.empty()));
    assertThat(nc.getInterface("foo", "eth0").orElse(null), equalTo(i));
  }
}
