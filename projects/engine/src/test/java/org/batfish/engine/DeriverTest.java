package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.engine.Deriver.Change;
import org.batfish.main.Batfish;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Proposed config lines, per dialect, have the effect the device would give them. */
public final class DeriverTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private EngineBatfish _engine;
  private SnapshotBuilder _builder;
  private Deriver _deriver;

  @Before
  public void setUp() throws Exception {
    _engine = new EngineBatfish(_folder.newFolder().toPath(), 8);
    _builder = new SnapshotBuilder(_engine);
    _deriver = new Deriver(_builder);
  }

  private DataPlane dataPlane(SnapshotBuilder.Built built) {
    Batfish bf = _engine.batfish(built.id(), null);
    bf.computeDataPlane(EngineBatfish.snapshot(built.id()));
    return bf.loadDataPlane(EngineBatfish.snapshot(built.id()));
  }

  private static Set<AbstractRoute> routes(DataPlane dp, String node, String prefix) {
    return dp.getRibs().get(node, Configuration.DEFAULT_VRF_NAME).getRoutes(Prefix.parse(prefix));
  }

  private Deriver.Derived derive(SnapshotRequest base, Change... changes) {
    return _deriver.derive(_builder.build(base), ImmutableList.copyOf(changes));
  }

  @Test
  public void frrExplicitNoEnforceFirstAsRetainsRoutingWithoutCoverageHoles() {
    Deriver.Derived derived =
        derive(
            Fixtures.frrOspfBgp(),
            new Change("r1", List.of("router bgp 65001", "no bgp enforce-first-as")));
    assertThat(derived.introduced(), empty());
    assertThat(routes(dataPlane(derived.snapshot()), "r2", "10.1.0.0/24"), hasSize(1));
  }

  @Test
  public void frrRemovingStaticDiscardRestoresLearnedReturnRoute() {
    SnapshotRequest base = Fixtures.frrOspfBgp();
    Deriver.Derived blocked = derive(base, new Change("r2", List.of("ip route 10.1.0.0/24 Null0")));
    assertThat(blocked.introduced(), empty());
    assertThat(
        routes(dataPlane(blocked.snapshot()), "r2", "10.1.0.0/24").stream()
            .allMatch(route -> route.getProtocol() == RoutingProtocol.STATIC),
        equalTo(true));
    Deriver.Derived repaired =
        derive(
            base,
            new Change(
                "r2", List.of("ip route 10.1.0.0/24 Null0", "no ip route 10.1.0.0/24 Null0")));
    assertThat(repaired.introduced(), empty());
    Set<AbstractRoute> restored = routes(dataPlane(repaired.snapshot()), "r2", "10.1.0.0/24");
    assertThat(restored, hasSize(1));
    assertThat(
        restored.stream().noneMatch(route -> route.getProtocol() == RoutingProtocol.STATIC),
        equalTo(true));
    Deriver.Derived absent =
        derive(base, new Change("r2", List.of("no ip route 10.1.0.0/24 Null0")));
    assertThat(absent.introduced(), empty());
    assertThat(routes(dataPlane(absent.snapshot()), "r2", "10.1.0.0/24"), hasSize(1));
  }

  @Test
  public void frrNoShutdownRestoresInterface() {
    Deriver.Derived d =
        derive(
            Fixtures.frrOspfBgp(),
            new Change("r1", List.of("interface eth1", "shutdown", "no shutdown")));
    assertThat(d.introduced(), empty());
    assertThat(
        d.snapshot().facts().get("r1").interfaces().stream()
            .filter(i -> i.name().equals("eth1"))
            .findFirst()
            .orElseThrow()
            .adminUp(),
        equalTo(true));
  }

  @Test
  public void frrRepairChangesAdministrativeStateButPreservesPhysicalOutages() {
    SnapshotRequest original = Fixtures.frrOspfBgp();
    List<SnapshotRequest.Device> devices =
        original.devices().stream()
            .map(
                d ->
                    new SnapshotRequest.Device(
                        d.name(),
                        d.dialect(),
                        d.configText(),
                        d.linuxInterfaces().stream()
                            .map(
                                i ->
                                    new SnapshotRequest.LinuxInterface(
                                        i.name(),
                                        i.addresses(),
                                        i.up()
                                            && !(d.name().equals("r1") && i.name().equals("eth1"))))
                            .toList()))
            .toList();
    Change repair = new Change("r1", List.of("interface eth1", "no shutdown"));
    SnapshotBuilder.Built base =
        _builder.build(
            new SnapshotRequest(devices, original.layer1(), SnapshotRequest.Environment.NONE));
    assertThat(
        base.facts().get("r1").interfaces().stream()
            .filter(i -> i.name().equals("eth1"))
            .findFirst()
            .orElseThrow()
            .adminUp(),
        equalTo(false));
    Deriver.Derived repaired = _deriver.derive(base, List.of(repair));
    assertThat(repaired.introduced(), empty());
    assertThat(
        repaired.snapshot().facts().get("r1").interfaces().stream()
            .filter(i -> i.name().equals("eth1"))
            .findFirst()
            .orElseThrow()
            .active(),
        equalTo(true));
    assertThat(routes(dataPlane(repaired.snapshot()), "r2", "10.1.0.0/24"), hasSize(1));
    Deriver.Derived physicallyDown =
        derive(
            new SnapshotRequest(
                devices,
                original.layer1(),
                new SnapshotRequest.Environment(
                    Set.of(new SnapshotRequest.NodeInterface("r1", "eth1")), Set.of())),
            repair);
    assertThat(
        physicallyDown.snapshot().facts().get("r1").interfaces().stream()
            .filter(i -> i.name().equals("eth1"))
            .findFirst()
            .orElseThrow()
            .active(),
        equalTo(false));
  }

  @Test
  public void applyInsertsBeforeTrailingEnd() {
    assertThat(
        Deriver.apply("hostname a\n!\nend\n", ImmutableList.of("ip routing")),
        equalTo("hostname a\n!\nip routing\nend\n"));
    assertThat(
        Deriver.apply("set system host-name a", ImmutableList.of("set x")),
        equalTo("set system host-name a\nset x\n"));
  }

  /** Batfish's FRR grammar has no {@code neighbor shutdown}: the change must come back unknown. */
  @Test
  public void frrNeighborShutdownIsUnknown() {
    Deriver.Derived d =
        derive(
            Fixtures.frrOspfBgp(),
            new Change("r1", List.of("router bgp 65001", " neighbor 10.0.0.2 shutdown")));

    assertThat(
        d.introduced().stream().map(h -> h.family() + "/" + h.kind() + "/" + h.text()).toList(),
        equalTo(List.of("bgp/unrecognized/neighbor 10.0.0.2 shutdown")));
  }

  @Test
  public void frrWithdrawNetworkRemovesOnlyTheAdvertisement() {
    SnapshotBuilder.Built base = _builder.build(Fixtures.frrOspfBgp());
    assertThat(routes(dataPlane(base), "r2", "10.1.0.0/24"), hasSize(1));
    Deriver.Derived withdrawn =
        _deriver.derive(
            base,
            List.of(
                new Change(
                    "r1",
                    List.of(
                        "router bgp 65001",
                        "address-family ipv4 unicast",
                        "no network 10.1.0.0/24",
                        "exit-address-family",
                        "exit"))));
    assertThat(withdrawn.introduced(), empty());
    assertThat(routes(dataPlane(withdrawn.snapshot()), "r2", "10.1.0.0/24"), empty());
    assertThat(routes(dataPlane(withdrawn.snapshot()), "r1", "10.1.0.0/24"), hasSize(1));
    assertThat(routes(dataPlane(withdrawn.snapshot()), "r2", "1.1.1.1/32"), hasSize(1));
    Deriver.Derived restored =
        _deriver.derive(
            withdrawn.snapshot(),
            List.of(
                new Change(
                    "r1",
                    List.of(
                        "router bgp 65001",
                        "address-family ipv4 unicast",
                        "network 10.1.0.0/24",
                        "exit-address-family",
                        "exit"))));
    assertThat(restored.introduced(), empty());
    assertThat(routes(dataPlane(restored.snapshot()), "r2", "10.1.0.0/24"), hasSize(1));
  }

  @Test
  public void frrNewDenyClauseStopsExport() {
    Deriver.Derived d =
        derive(Fixtures.frrOspfBgp(), new Change("r1", List.of("route-map EXPORT-SITE deny 5")));

    assertThat(d.introduced(), empty());
    DataPlane dp = dataPlane(d.snapshot());
    assertThat(routes(dp, "r2", "10.1.0.0/24"), empty());
    assertThat(routes(dp, "r2", "1.1.1.1/32"), hasSize(1));
  }

  /** Batfish's FRR grammar cannot remove a route-map clause: the change must come back unknown. */
  @Test
  public void frrRemovingAClauseIsUnknown() {
    Deriver.Derived d =
        derive(
            Fixtures.frrOspfBgp(), new Change("r1", List.of("no route-map EXPORT-SITE permit 10")));

    assertThat(
        d.introduced().stream().map(h -> h.family() + "/" + h.kind()).toList(),
        equalTo(List.of("policy/unrecognized")));
  }

  /** Batfish silently keeps the original clause, so the deriver must flag the redeclaration. */
  @Test
  public void redeclaringAClauseWithTheOtherActionIsUnknown() {
    Deriver.Derived d =
        derive(Fixtures.frrOspfBgp(), new Change("r1", List.of("route-map EXPORT-SITE deny 10")));

    assertThat(
        d.introduced().stream().map(h -> h.family() + "/" + h.kind()).toList(),
        equalTo(List.of("policy/redeclared-clause")));
  }

  @Test
  public void eosInterfaceShutdownDropsOspf() {
    Deriver.Derived d =
        derive(
            Fixtures.dc1Eos(), new Change("core2", List.of("interface Ethernet48", "   shutdown")));

    assertThat(d.introduced(), empty());
    assertThat(routes(dataPlane(d.snapshot()), "core1", "2.2.2.2/32"), empty());
  }

  @Test
  public void eosStaticRouteAddAndRemove() {
    SnapshotBuilder.Built base = _builder.build(Fixtures.dc1Eos());
    Deriver.Derived added =
        _deriver.derive(base, List.of(new Change("core1", List.of("ip route 10.9.0.0/16 Null0"))));
    assertThat(added.introduced(), empty());
    assertThat(
        routes(dataPlane(added.snapshot()), "core1", "10.9.0.0/16").stream()
            .map(AbstractRoute::getProtocol)
            .toList(),
        equalTo(List.of(RoutingProtocol.STATIC)));

    Deriver.Derived removed =
        _deriver.derive(
            added.snapshot(),
            List.of(new Change("core1", List.of("no ip route 10.9.0.0/16 Null0"))));
    assertThat(removed.introduced(), empty());
    assertThat(routes(dataPlane(removed.snapshot()), "core1", "10.9.0.0/16"), empty());
  }

  @Test
  public void junosDeleteExportPolicyStopsAdvertisement() {
    Deriver.Derived d =
        derive(
            Fixtures.crpd(),
            new Change("r1", List.of("delete protocols bgp group ebgp export export-site")));

    assertThat(d.introduced(), empty());
    assertThat(routes(dataPlane(d.snapshot()), "r2", "10.1.0.0/24"), empty());
  }

  @Test
  public void unmodeledLineMakesTheChangeUnknown() {
    Deriver.Derived d =
        derive(
            Fixtures.eosPolicy(),
            new Change(
                "edge1",
                List.of(
                    "router bgp 65001",
                    "   bgp always-compare-med",
                    "   bgp bestpath med missing-as-worst")));

    assertThat(d.introduced(), not(empty()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void changeForUnknownDeviceIsRejected() {
    derive(Fixtures.crpd(), new Change("r9", List.of("set system host-name r9")));
  }
}
