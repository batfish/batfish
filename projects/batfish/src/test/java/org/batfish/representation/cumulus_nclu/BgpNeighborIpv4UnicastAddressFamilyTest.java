package org.batfish.representation.cumulus_nclu;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/** Tests of{@link BgpNeighborIpv4UnicastAddressFamily} */
public class BgpNeighborIpv4UnicastAddressFamilyTest {

  private BgpNeighborIpv4UnicastAddressFamily _empty;
  private BgpNeighborIpv4UnicastAddressFamily _withSettings;

  @Before
  public void setUp() {
    _empty = new BgpNeighborIpv4UnicastAddressFamily();
    _withSettings = new BgpNeighborIpv4UnicastAddressFamily();
    _withSettings.setActivated(true);
    _withSettings.setAllowAsIn(5);
    _withSettings.setDefaultOriginate(true);
    _withSettings.setRouteReflectorClient(false);
  }

  @Test
  public void testInheritFromSetSettings() {
    // Inherit settings correctly
    _empty.inheritFrom(_withSettings);
    assertTrue(_empty.getActivated());
    assertEquals(_empty.getAllowAsIn().intValue(), 5);
    assertTrue(_empty.getDefaultOriginate());
    assertFalse(_empty.getRouteReflectorClient());
  }

  @Test
  public void testInheritUnsetFromUnset() {
    // Inheriting unset settings does not override existing settings
    _empty.inheritFrom(_empty);
    assertThat(_empty.getActivated(), nullValue());
    assertThat(_empty.getAllowAsIn(), nullValue());
    assertThat(_empty.getDefaultOriginate(), nullValue());
    assertThat(_empty.getRouteReflectorClient(), nullValue());
  }

  @Test
  public void testInheritUnsetFromSetDoesNotOverride() {
    // Inheriting unset settings onto existing settings does override
    _withSettings.inheritFrom(_empty);
    assertThat(_withSettings.getActivated(), equalTo(_withSettings.getActivated()));
    assertThat(_withSettings.getAllowAsIn(), equalTo(_withSettings.getAllowAsIn()));
    assertThat(_withSettings.getDefaultOriginate(), equalTo(_withSettings.getDefaultOriginate()));
    assertThat(
        _withSettings.getRouteReflectorClient(), equalTo(_withSettings.getRouteReflectorClient()));
  }
}
