package org.batfish.bddreachability;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.util.TracePruner;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.flow.Trace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.multipath.MultipathConsistencyParameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BDDMCPTest {

  @Rule public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void testLoopInMultipathConsistency() throws IOException {
    SortedMap<String, Configuration> configurations =
        new TestNetworkWithMPCandLoop()._configurations;
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane(false);

    MultipathConsistencyParameters multipathConsistencyParameters =
        new MultipathConsistencyParameters(
            AclLineMatchExprs.TRUE,
            batfish.getAllSourcesInferFromLocationIpSpaceAssignment(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            TracePruner.DEFAULT_MAX_TRACES,
            ImmutableSet.of());
    Set<Flow> flows = batfish.bddMultipathConsistency(multipathConsistencyParameters);

    Map<Flow, List<Trace>> flowTraes =
        batfish.buildFlows(
            ImmutableSet.of(
                Flow.builder()
                    .setIngressNode("configuration1")
                    .setDstIp(new Ip("2.2.2.2"))
                    .setTag("test")
                    .build()),
            false);
    System.out.print("aha");
  }

  @Test
  public void testTraceroute() throws IOException {
    SortedMap<String, Configuration> configurations =
        new TestNetworkWithMPCandLoop()._configurations;
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane(false);

    Map<Flow, List<Trace>> flowTraes =
        batfish.buildFlows(
            ImmutableSet.of(
                Flow.builder()
                    .setIngressNode("configuration1")
                    .setDstIp(new Ip("2.2.2.2"))
                    .setTag("test")
                    .setIngressVrf("~Vrf_0~")
                    .build()),
            false);
    System.out.print("aha");
  }

  @Test
  public void testReachability() throws IOException {
    SortedMap<String, Configuration> configurations =
        new TestNetworkWithMPCandLoop()._configurations;
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane(false);

    Map<Flow, List<Trace>> flowTraes =
        batfish.buildFlows(
            ImmutableSet.of(
                Flow.builder()
                    .setIngressNode("configuration1")
                    .setDstIp(new Ip("2.2.2.2"))
                    .setTag("test")
                    .setIngressVrf("~Vrf_0~")
                    .build()),
            false);
    System.out.print("aha");
  }
}
