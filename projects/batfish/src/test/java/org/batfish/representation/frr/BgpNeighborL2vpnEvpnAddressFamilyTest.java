package org.batfish.representation.frr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests of {@link BgpNeighborL2vpnEvpnAddressFamily} */
class BgpNeighborL2vpnEvpnAddressFamilyTest {
  private BgpNeighborL2vpnEvpnAddressFamily _empty;
  private BgpNeighborL2vpnEvpnAddressFamily _withSettings;

  @BeforeEach
  void setUp() {
    _empty = new BgpNeighborL2vpnEvpnAddressFamily();
    _withSettings = new BgpNeighborL2vpnEvpnAddressFamily();
    _withSettings.setActivated(true);
    _withSettings.setRouteReflectorClient(false);
  }

  @Test
  void testInheritFromSetSettings() {
    // Inherit settings correctly
    _empty.inheritFrom(_withSettings);
    assertTrue(_empty.getActivated());
    assertFalse(_empty.getRouteReflectorClient());
  }

  @Test
  void testInheritUnsetFromUnset() {
    // Inheriting unset settings does not override existing settings
    _empty.inheritFrom(_empty);
    assertThat(_empty.getActivated(), nullValue());
    assertThat(_empty.getRouteReflectorClient(), nullValue());
  }

  @Test
  void testInheritUnsetFromSetDoesNotOverride() {
    // Inheriting unset settings onto existing settings does override
    _withSettings.inheritFrom(_empty);
    assertThat(_withSettings.getActivated(), equalTo(_withSettings.getActivated()));
    assertThat(
        _withSettings.getRouteReflectorClient(), equalTo(_withSettings.getRouteReflectorClient()));
  }
}
