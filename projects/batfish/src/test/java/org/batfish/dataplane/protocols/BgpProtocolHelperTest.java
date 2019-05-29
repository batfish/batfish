package org.batfish.dataplane.protocols;

import static org.batfish.dataplane.protocols.BgpProtocolHelper.transformBgpRouteOnImport;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Bgpv4Route.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.junit.Before;
import org.junit.Test;

public class BgpProtocolHelperTest {
  private static final Ip DEST_IP = Ip.parse("3.3.3.3");
  private static final Prefix DEST_NETWORK = Prefix.parse("4.4.4.0/24");
  private static final Ip ORIGINATOR_IP = Ip.parse("1.1.1.1");
  private final BgpProcess _process =
      BgpProcess.builder()
          .setAdminCostsToVendorDefaults(ConfigurationFormat.CISCO_IOS)
          .setRouterId(ORIGINATOR_IP)
          .build();
  private Builder _baseBgpRouteBuilder;

  /** Reset route builder */
  @Before
  public void resetDefaultRouteBuilders() {
    _baseBgpRouteBuilder =
        new Bgpv4Route.Builder()
            .setOriginatorIp(ORIGINATOR_IP)
            .setOriginType(OriginType.IGP)
            .setNetwork(DEST_NETWORK)
            .setNextHopIp(DEST_IP)
            .setProtocol(RoutingProtocol.IBGP)
            .setReceivedFromIp(Ip.ZERO);
  }

  @Test
  public void testTransformOnImportNoAllowAsIn() {
    assertThat(
        "AS path loop, return null",
        transformBgpRouteOnImport(
            _baseBgpRouteBuilder.setAsPath(AsPath.ofSingletonAsSets(1L)).build(),
            1L,
            false,
            true,
            _process,
            null),
        nullValue());
  }

  @Test
  public void testTransformOnImportAllowAsIn() {
    assertThat(
        "AS path loop allowed",
        transformBgpRouteOnImport(
            _baseBgpRouteBuilder.setAsPath(AsPath.ofSingletonAsSets(1L)).build(),
            1L,
            true,
            true,
            _process,
            null),
        notNullValue());
  }

  @Test
  public void testTransformOnImportEbgp() {
    assertThat(
        "No AS path loop, eBGP",
        transformBgpRouteOnImport(
                _baseBgpRouteBuilder.setAsPath(AsPath.ofSingletonAsSets(1L)).build(),
                2L,
                false,
                true,
                _process,
                null)
            .getProtocol(),
        equalTo(RoutingProtocol.BGP));
  }

  @Test
  public void testTransformOnImportIbgp() {
    assertThat(
        "No AS path loop, iBGP",
        transformBgpRouteOnImport(_baseBgpRouteBuilder.build(), 1L, false, false, _process, null)
            .getProtocol(),
        equalTo(RoutingProtocol.IBGP));
  }

  @Test
  public void testTransformOnImportClearAdminSetInterface() {
    final Builder builder =
        transformBgpRouteOnImport(
            _baseBgpRouteBuilder.setAdmin(Integer.MAX_VALUE).build(),
            2L,
            false,
            true,
            _process,
            "eth0");
    assertThat("PeerInterface is set", builder.getNextHopInterface(), equalTo("eth0"));
    assertThat(
        "AdminDistance is set",
        builder.getAdmin(),
        equalTo(RoutingProtocol.BGP.getDefaultAdministrativeCost(ConfigurationFormat.CISCO_IOS)));
  }
}
