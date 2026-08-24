package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.Ospfv3Authentication;
import org.batfish.datamodel.ospf.Ospfv3InterfaceSettings;
import org.junit.Test;

/** Tests for {@link Ospfv3InterfaceSettings}. */
public final class Ospfv3InterfaceSettingsTest {

  @Test
  public void testDefaults() {
    Ospfv3InterfaceSettings settings =
        Ospfv3InterfaceSettings
            .defaultSettingsBuilder()
            .setAreaName(0L)
            .setProcess("1")
            .build();

    assertThat(
        settings.getAuthentication(),
        equalTo(null));

    assertThat(
        settings.getBfdEnabled(),
        equalTo(false));

    assertThat(
        settings.getPriority(),
        equalTo(
            Ospfv3InterfaceSettings
                .DEFAULT_PRIORITY));

    assertThat(
        settings.getRetransmitInterval(),
        equalTo(
            Ospfv3InterfaceSettings
                .DEFAULT_RETRANSMIT_INTERVAL));

    assertThat(
        settings.getTransitDelay(),
        equalTo(
            Ospfv3InterfaceSettings
                .DEFAULT_TRANSIT_DELAY));
  }

  @Test
  public void testSerialization() {
    Ospfv3InterfaceSettings settings =
        Ospfv3InterfaceSettings.builder()
            .setAreaName(7L)
            .setAuthentication(
                new Ospfv3Authentication(
                    300L,
                    Ospfv3Authentication.AuthType.SHA1,
                    Ospfv3Authentication.KeyType.PLAINTEXT,
                    "test-secret"))
            .setBfdEnabled(true)
            .setCost(25)
            .setDeadInterval(40)
            .setEnabled(true)
            .setHelloInterval(10)
            .setNetworkType(
                OspfNetworkType.BROADCAST)
            .setPassive(false)
            .setPriority(200)
            .setProcess("7")
            .setRetransmitInterval(17)
            .setTransitDelay(9)
            .build();

    Ospfv3InterfaceSettings clone =
        BatfishObjectMapper.clone(
            settings,
            Ospfv3InterfaceSettings.class);

    assertThat(
        clone,
        equalTo(settings));

    assertThat(
        clone.getAuthentication(),
        equalTo(
            new Ospfv3Authentication(
                300L,
                Ospfv3Authentication.AuthType.SHA1,
                Ospfv3Authentication.KeyType.PLAINTEXT,
                "test-secret")));

    assertThat(
        clone.getBfdEnabled(),
        equalTo(true));

    assertThat(
        clone.getPriority(),
        equalTo(200));

    assertThat(
        clone.getRetransmitInterval(),
        equalTo(17));

    assertThat(
        clone.getTransitDelay(),
        equalTo(9));
  }
}
