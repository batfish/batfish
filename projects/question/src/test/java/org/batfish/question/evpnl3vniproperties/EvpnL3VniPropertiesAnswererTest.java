package org.batfish.question.evpnl3vniproperties;

import static org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesAnswerer.COL_EXPORT_ROUTE_TARGET;
import static org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesAnswerer.COL_IMPORT_ROUTE_TARGET;
import static org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesAnswerer.COL_NODE;
import static org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesAnswerer.COL_ROUTE_DISTINGUISHER;
import static org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesAnswerer.COL_VNI;
import static org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesAnswerer.COL_VRF;
import static org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesAnswerer.createTableMetadata;
import static org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesAnswerer.generateRow;
import static org.batfish.question.evpnl3vniproperties.EvpnL3VniPropertiesAnswerer.getRows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Collections;
import java.util.Map;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Layer3VniConfig;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

/** Test for {@link EvpnL3VniPropertiesAnswerer} */
public class EvpnL3VniPropertiesAnswererTest {
  @Test
  public void testGenerateRow() {
    String node = "n";
    int vni = 100001;
    Layer3VniConfig vniConfig =
        Layer3VniConfig.builder()
            .setVni(vni)
            .setVrf("vrf")
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.ZERO, 2))
            .setRouteTarget(ExtendedCommunity.target(65500, vni))
            .setImportRouteTarget(ExtendedCommunity.target(65500, vni).matchString())
            .setAdvertiseV4Unicast(false)
            .build();
    Map<String, ColumnMetadata> columnMap =
        createTableMetadata(new EvpnL3VniPropertiesQuestion(null)).toColumnMap();
    assertThat(
        generateRow(vniConfig, node, columnMap),
        equalTo(
            Row.builder(columnMap)
                .put(COL_NODE, node)
                .put(COL_VRF, vniConfig.getVrf())
                .put(COL_VNI, vniConfig.getVni())
                .put(COL_EXPORT_ROUTE_TARGET, vniConfig.getRouteTarget().matchString())
                .put(COL_IMPORT_ROUTE_TARGET, vniConfig.getImportRouteTarget())
                .put(COL_ROUTE_DISTINGUISHER, vniConfig.getRouteDistinguisher())
                .build()));
  }

  /**
   * Ensure we get one row per L3 Vni depsite multiple peers having the {@link Layer3VniConfig} for
   * it
   */
  @Test
  public void testVniConfigDeDuping() {
    NetworkFactory nf = new NetworkFactory();
    String host = "n";
    Configuration c1 =
        nf.configurationBuilder()
            .setHostname(host)
            .setConfigurationFormat(ConfigurationFormat.ARISTA)
            .build();
    nf.vrfBuilder().setName("vrf1").setOwner(c1).build();
    Vrf defaultVrf = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c1).build();
    Ip localIp = Ip.parse("1.1.1.1");
    BgpProcess bgpProc = BgpProcess.testBgpProcess(localIp);
    defaultVrf.setBgpProcess(bgpProc);
    int vni = 100001;
    Layer3VniConfig vniConfig =
        Layer3VniConfig.builder()
            .setVni(vni)
            .setVrf("vrf")
            .setRouteDistinguisher(RouteDistinguisher.from(Ip.ZERO, 2))
            .setRouteTarget(ExtendedCommunity.target(65500, vni))
            .setImportRouteTarget(ExtendedCommunity.target(65500, vni).matchString())
            .setAdvertiseV4Unicast(false)
            .build();
    BgpActivePeerConfig peer1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("2.2.2.2"))
            .setRemoteAs(1L)
            .setLocalIp(localIp)
            .setLocalAs(2L)
            .setEvpnAddressFamily(
                EvpnAddressFamily.builder()
                    .setL2Vnis(ImmutableSet.of())
                    .setL3Vnis(ImmutableSet.of(vniConfig))
                    .setPropagateUnmatched(true)
                    .build())
            .build();
    BgpActivePeerConfig peer2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("2.2.2.3"))
            .setRemoteAs(1L)
            .setLocalIp(localIp)
            .setLocalAs(2L)
            .setEvpnAddressFamily(
                EvpnAddressFamily.builder()
                    .setL2Vnis(ImmutableSet.of())
                    .setL3Vnis(ImmutableSet.of(vniConfig))
                    .setPropagateUnmatched(true)
                    .build())
            .build();
    bgpProc.setNeighbors(
        ImmutableSortedMap.of(peer1.getPeerAddress(), peer1, peer2.getPeerAddress(), peer2));

    ImmutableMap<String, Configuration> configs = ImmutableMap.of(c1.getHostname(), c1);
    Map<String, ColumnMetadata> columnMap =
        createTableMetadata(new EvpnL3VniPropertiesQuestion(null)).toColumnMap();

    NetworkConfigurations nc = NetworkConfigurations.of(configs);
    assertThat(
        getRows(Collections.singleton(c1.getHostname()), nc, columnMap),
        equalTo(
            ImmutableSet.of(
                Row.builder(columnMap)
                    .put(COL_NODE, c1.getHostname())
                    .put(COL_VRF, vniConfig.getVrf())
                    .put(COL_VNI, vniConfig.getVni())
                    .put(COL_EXPORT_ROUTE_TARGET, vniConfig.getRouteTarget().matchString())
                    .put(COL_IMPORT_ROUTE_TARGET, vniConfig.getImportRouteTarget())
                    .put(COL_ROUTE_DISTINGUISHER, vniConfig.getRouteDistinguisher())
                    .build())));
    assertThat(getRows(Collections.singleton("nonexistent"), nc, columnMap), empty());
  }
}
