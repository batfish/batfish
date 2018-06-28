package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

/** Tests of {@link NetworkConfigurations} */
public class NetworkConfigurationsTest {

  @Test
  public void testWrapConfigs() {
    NetworkConfigurations nc = NetworkConfigurations.of(ImmutableMap.of());
    assertThat(nc.getMap(), anEmptyMap());
    assertThat(nc.get("foo"), nullValue());
  }

  @Test
  public void testGetVrf() {
    Configuration c = new Configuration("foo", ConfigurationFormat.CISCO_IOS);
    c.getVrfs().put("fooVRF", new Vrf("fooVRF"));
    NetworkConfigurations nc = NetworkConfigurations.of(ImmutableMap.of("foo", c));

    // Missing values are null
    assertThat(nc.getVrf("missingHostname", "missingVRF"), nullValue());
    assertThat(nc.getVrf("missingHostname", "fooVRF"), nullValue());
    assertThat(nc.getVrf("foo", "missingVRF"), nullValue());

    // Actual get succeeds
    assertThat(nc.getVrf("foo", "fooVRF"), notNullValue());
  }
}
