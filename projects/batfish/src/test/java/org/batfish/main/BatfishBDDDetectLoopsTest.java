package org.batfish.main;

import static org.batfish.datamodel.FlowDisposition.LOOP;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.flow.Trace;
import org.batfish.dataplane.TracerouteEngineImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BatfishBDDDetectLoopsTest {
  @Rule public TemporaryFolder _tempFolder = new TemporaryFolder();

  private Batfish _batfish;
  private Configuration _c1;
  private Vrf _v1;
  private Prefix _loopPrefix;

  private void initNetwork(boolean includeLoop) throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _c1 = cb.build();
    _v1 = nf.vrfBuilder().setOwner(_c1).build();
    InterfaceAddress c1Addr = new InterfaceAddress("1.0.0.0/31");
    InterfaceAddress c2Addr = new InterfaceAddress("1.0.0.1/31");
    Interface i1 =
        nf.interfaceBuilder().setActive(true).setOwner(_c1).setVrf(_v1).setAddress(c1Addr).build();
    _loopPrefix = Prefix.parse("2.0.0.0/32");
    _v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(_loopPrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(i1.getName())
                .setNextHopIp(c2Addr.getIp())
                .build()));
    Configuration c2 = cb.build();
    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    Interface i2 =
        nf.interfaceBuilder().setActive(true).setOwner(c2).setVrf(v2).setAddress(c2Addr).build();
    Prefix natPoolIp = Prefix.parse("5.5.5.5/32");

    if (!includeLoop) {
      // stop the loop by adding an ingress ACL that filters NATted traffic
      i2.setIncomingFilter(
          nf.aclBuilder()
              .setOwner(c2)
              .setLines(
                  ImmutableList.of(
                      IpAccessListLine.rejecting(AclLineMatchExprs.matchSrc(natPoolIp))))
              .build());
    }

    i2.setSourceNats(
        ImmutableList.of(
            SourceNat.builder()
                .setAcl(
                    nf.aclBuilder()
                        .setOwner(c2)
                        .setLines(ImmutableList.of(IpAccessListLine.ACCEPT_ALL))
                        .build())
                .setPoolIpFirst(natPoolIp.getStartIp())
                .setPoolIpLast(natPoolIp.getStartIp())
                .build()));
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(_loopPrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(i2.getName())
                .setNextHopIp(c1Addr.getIp())
                .build()));
    _batfish =
        BatfishTestUtils.getBatfish(
            ImmutableSortedMap.of(_c1.getHostname(), _c1, c2.getHostname(), c2), _tempFolder);
    _batfish.computeDataPlane(false);
  }

  /*
   * Create a network with a forwarding loop. When we run with ACLs enabled, the loop is detected.
   * When we run with ACLs enabled, it's not an infinite loop: we apply source NAT in the first
   * iteration, and drop with an ingress ACL in the second iteration.
   */
  @Test
  public void testLoop() throws IOException {
    initNetwork(false);
    Set<Flow> flows = _batfish.bddLoopDetection();
    assertThat(flows, empty());

    initNetwork(true);
    flows = _batfish.bddLoopDetection();
    assertThat(flows, hasSize(2));

    DataPlane dp = _batfish.loadDataPlane();
    SortedMap<Flow, List<Trace>> flowTraces =
        TracerouteEngineImpl.getInstance().buildFlows(dp, flows, dp.getFibs(), false);
    Set<FlowDisposition> dispositions =
        flowTraces
            .values()
            .stream()
            .flatMap(Collection::stream)
            .map(Trace::getDisposition)
            .collect(Collectors.toSet());
    assertThat(dispositions, equalTo(ImmutableSet.of(LOOP)));
  }
}
