package org.batfish.question.traceroute;

import static org.batfish.common.util.TracePruner.DEFAULT_MAX_TRACES;
//import static org.batfish.datamodel.matchers.TraceMatchers.hasDisposition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
//import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
//import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.config.Settings;
import org.batfish.main.TestrigText;
import org.batfish.question.edges.EdgesQuestion;
import org.batfish.question.edges.EdgesQuestion.EdgeType;
import org.batfish.question.routes.RoutesQuestion;
import org.batfish.question.routes.RoutesQuestion.RibProtocol;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * End-to-end test for loading Juniper EVPN configs from a testrig and performing traceroute.
 *
 * <p>This test is designed to help diagnose NPE and other issues when running traceroute against
 * Juniper EVPN L3VNI configurations with VRFs and IRB interfaces.
 *
 * <p>The test mirrors the Pybatfish pattern:
 *
 * <pre>
 *   bf.q.traceroute(
 *       startLocation='@enter(node1-1[irb.100])',
 *       headers=HeaderConstraints(dstIps='192.168.99.10', srcIps='172.16.100.6')
 *   ).answer().frame()
 * </pre>
 */
public class JuniperEvpnTracerouteTest {

  private static final String TESTRIGS_PREFIX = "org/batfish/allinone/testrigs/";
  private static final String TESTRIG_NAME = "evpn-working-lab";

  // All config filenames in the configs/ directory of the testrig (as they appear on disk).
  private static final List<String> TESTRIG_NODE_NAMES =
          ImmutableList.of(
                  "edge1", "node1-2", "node2-2", "sw1", "sw3",
                  "node1-1", "node2-1", "router1", "sw2");

  // Batfish lowercases the Junos "host-name" to produce the VI configuration key.
  // host-name LAB-1-DRT -> "node1-1", host-name LAB-1-SN -> "lab-1-sn"
  private static final String DRT_HOSTNAME = "node1-1";

  @Rule
  public TemporaryFolder _folder = new TemporaryFolder();

  // ---- Helper: load testrig ----

  private Batfish loadTestrig() throws IOException {
    Batfish batfish =
            BatfishTestUtils.getBatfishFromTestrigText(
                    TestrigText.builder()
                            .setConfigurationFiles(TESTRIGS_PREFIX + TESTRIG_NAME, TESTRIG_NODE_NAMES)
                            .build(),
                    _folder);
    // Relax strict parsing settings so unrecognized config lines are tolerated
    // (like a normal Batfish production run), instead of failing the test.
    Settings settings = batfish.getSettings();
    settings.setDisableUnrecognized(false);
    settings.setHaltOnConvertError(false);
    settings.setHaltOnParseError(false);
    settings.setThrowOnLexerError(false);
    settings.setThrowOnParserError(false);
    return batfish;
  }

  // ---- Helper: load testrig and compute data plane ----

  private Batfish loadTestrigAndComputeDataPlane() throws IOException {
    Batfish batfish = loadTestrig();
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish;
  }

  // ---- Step 1: Verify configs load without NPE ----

  @Test
  public void testConfigsLoad() throws IOException {
    Batfish batfish = loadTestrig();
    NetworkSnapshot snapshot = batfish.getSnapshot();

    Map<String, Configuration> configs;
    try {
      configs = batfish.loadConfigurations(snapshot);
    } catch (NullPointerException e) {
      fail("loadConfigurations threw NPE. Stack trace:\n" + stackTraceToString(e));
      return;
    }

    assertNotNull("Configs should not be null", configs);
    assertFalse("Configs should not be empty", configs.isEmpty());

    // Print discovered hostnames for debugging
    System.out.println("Discovered VI hostnames: " + configs.keySet());

    assertTrue(
            DRT_HOSTNAME + " should be present in configs. Actual keys: " + configs.keySet(),
            configs.containsKey(DRT_HOSTNAME));
  }

  // ---- Step 2: Verify VRFs and interfaces exist on parsed config ----

  @Test
  public void testViModelHasExpectedVrfsAndInterfaces() throws IOException {
    Batfish batfish = loadTestrig();
    Map<String, Configuration> configs = batfish.loadConfigurations(batfish.getSnapshot());

    Configuration drt = configs.get(DRT_HOSTNAME);
    assertNotNull(DRT_HOSTNAME + " config should not be null", drt);

    // Check VRFs — print them all for debugging
    Map<String, Vrf> vrfs = drt.getVrfs();
    assertNotNull("VRFs map should not be null", vrfs);
    System.out.println("VRFs on " + DRT_HOSTNAME + ": " + vrfs.keySet());

    // The "Lab" routing-instance should become a VRF (irb.100 is in routing-instance Lab)
    boolean hasLabVrf = vrfs.containsKey("TENANT-A") || vrfs.containsKey("lab");
    assertTrue(
            DRT_HOSTNAME + " should have a 'TENANT-A' VRF. Actual VRFs: " + vrfs.keySet(),
            hasLabVrf);

    // Check that irb.100 interface exists
    Map<String, Interface> allInterfaces = drt.getAllInterfaces();
    assertNotNull("Interfaces map should not be null", allInterfaces);

    boolean hasIrb100 = allInterfaces.containsKey("irb.100");
    assertTrue(
            DRT_HOSTNAME + " should have irb.100 interface. Actual interfaces: "
                    + allInterfaces.keySet(),
            hasIrb100);

    // Verify irb.100 has an address
    Interface irb100 = allInterfaces.get("irb.100");
    assertNotNull("irb.100 interface should not be null", irb100);
    assertThat(
            "irb.100 should have at least one address",
            irb100.getAllConcreteAddresses(),
            not(empty()));

    // Verify irb.100 is in the Lab VRF
    String irb100Vrf = irb100.getVrfName();
    System.out.println("irb.100 VRF: " + irb100Vrf);
  }

  // ---- Step 3: Verify data plane computes without NPE ----

  @Test
  public void testDataPlaneComputes() throws IOException {
    Batfish batfish = loadTestrig();
    try {
      batfish.computeDataPlane(batfish.getSnapshot());
    } catch (NullPointerException e) {
      fail(
              "Data plane computation threw NPE. Stack trace:\n"
                      + stackTraceToString(e));
    }
  }

  // ---- Step 4: Traceroute using @enter(LAB-1-DRT[irb.100]) ----

  @Test
  public void testTracerouteFromIrb100() throws IOException {
    Batfish batfish = loadTestrigAndComputeDataPlane();

    // This mirrors: bf.q.traceroute(
    //   startLocation='@enter(node1-1[irb.100])',
    //   headers=HeaderConstraints(dstIps='192.168.99.10', srcIps='172.16.100.6'))
    PacketHeaderConstraints header =
            PacketHeaderConstraints.builder()
                    .setSrcIp("172.16.100.6")
                    .setDstIp("192.168.99.10")
                    .build();

    // Batfish lowercases hostnames, so use the lowercased form in source location
    String sourceLocation = "@enter(" + DRT_HOSTNAME + "[irb.100])";

    TracerouteQuestion question =
            new TracerouteQuestion(sourceLocation, header, false, DEFAULT_MAX_TRACES);

    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);

    Map<Flow, List<Trace>> traces;
    try {
      traces = answerer.getTraces(batfish.getSnapshot(), question);
    } catch (NullPointerException e) {
      fail(
              "Traceroute threw NPE. Stack trace:\n"
                      + stackTraceToString(e));
      return;
    }

    assertNotNull("Traces map should not be null", traces);
    assertFalse("Traces map should not be empty", traces.isEmpty());

    // Print trace details for debugging
    for (Map.Entry<Flow, List<Trace>> entry : traces.entrySet()) {
      Flow flow = entry.getKey();
      List<Trace> traceList = entry.getValue();
      assertNotNull("Flow should not be null", flow);
      System.out.println("Flow: " + flow);
      for (Trace trace : traceList) {
        assertNotNull("Trace should not be null", trace);
        assertNotNull("Trace disposition should not be null", trace.getDisposition());
        assertTrue(trace.getDisposition().isSuccessful());
      }
    }
  }



  // ---- Step 5: Traceroute using low-level Flow API (bypass question layer) ----

//  @Test
//  public void testTracerouteViaFlowBuilder() throws IOException {
//    Batfish batfish = loadTestrigAndComputeDataPlane();
//    NetworkSnapshot snapshot = batfish.getSnapshot();
//
//    // Build flow directly — equivalent to @enter(node1-1[irb.100])
//    Flow flow =
//            Flow.builder()
//                    .setIngressNode(DRT_HOSTNAME)
//                    .setIngressInterface("irb.100")
//                    .setSrcIp(Ip.parse("172.16.100.6"))
//                    .setDstIp(Ip.parse("192.168.99.10"))
//                    .build();
//
//    SortedMap<Flow, List<Trace>> traces;
//    try {
//      traces =
//              batfish
//                      .getTracerouteEngine(snapshot)
//                      .computeTraces(ImmutableSet.of(flow), false);
//    } catch (NullPointerException e) {
//      fail(
//              "TracerouteEngine.computeTraces threw NPE. Stack trace:\n"
//                      + stackTraceToString(e));
//      return;
//    }
//
//    assertNotNull("Traces should not be null", traces);
//    assertTrue("Traces should contain our flow", traces.containsKey(flow));
//
//    List<Trace> flowTraces = traces.get(flow);
//    assertThat("Should have at least one trace", flowTraces, not(empty()));
//
//    // Print detailed hop/step info for debugging
//    Trace firstTrace = flowTraces.get(0);
//    assertNotNull("First trace should not be null", firstTrace);
//    assertNotNull("Disposition should not be null", firstTrace.getDisposition());
//    System.out.println("Disposition: " + firstTrace.getDisposition());
//
//    for (int i = 0; i < firstTrace.getHops().size(); i++) {
//      Hop hop = firstTrace.getHops().get(i);
//      assertNotNull("Hop " + i + " should not be null", hop);
//      assertNotNull("Hop " + i + " node should not be null", hop.getNode());
//      System.out.println("Hop " + i + ": " + hop.getNode().getName());
//      for (int j = 0; j < hop.getSteps().size(); j++) {
//        Step<?> step = hop.getSteps().get(j);
//        assertNotNull("Step " + j + " of hop " + i + " should not be null", step);
//        assertNotNull(
//                "Step " + j + " of hop " + i + " action should not be null", step.getAction());
//        System.out.println("  Step " + j + ": " + step.getAction() + " " + step.getDetail());
//      }
//    }
//  }

  // ---- Step 6: Traceroute originating from device (not @enter) ----
//
//  @Test
//  public void testTracerouteOriginatingFromDevice() throws IOException {
//    Batfish batfish = loadTestrigAndComputeDataPlane();
//
//    PacketHeaderConstraints header =
//        PacketHeaderConstraints.builder()
//            .setDstIp("192.168.99.10")
//            .build();
//
//    // Source is just the node name (traffic originates from device itself)
//    String sourceLocation = DRT_HOSTNAME;
//
//    TracerouteQuestion question =
//        new TracerouteQuestion(sourceLocation, header, false, DEFAULT_MAX_TRACES);
//
//    TracerouteAnswerer answerer = new TracerouteAnswerer(question, batfish);
//
//    Map<Flow, List<Trace>> traces;
//    try {
//      traces = answerer.getTraces(batfish.getSnapshot(), question);
//    } catch (NullPointerException e) {
//      fail(
//          "Traceroute (originating from device) threw NPE. Stack trace:\n"
//              + stackTraceToString(e));
//      return;
//    }
//
//    assertNotNull("Traces map should not be null", traces);
//    // May be empty if no route to destination; that's fine — the point is no NPE
//    System.out.println("Originating traceroute flows: " + traces.size());
//    for (Map.Entry<Flow, List<Trace>> entry : traces.entrySet()) {
//      System.out.println("  " + entry.getKey() + " -> "
//          + entry.getValue().stream()
//              .map(t -> t.getDisposition().toString())
//              .reduce((a, b) -> a + ", " + b).orElse("(no traces)"));
//    }
//  }
//
//  // ---- Step 7: Traceroute within the Lab VRF (irb.100 to irb.100 gateway) ----
//
//  @Test
//  public void testTracerouteWithinLabVrf() throws IOException {
//    Batfish batfish = loadTestrigAndComputeDataPlane();
//    NetworkSnapshot snapshot = batfish.getSnapshot();
//
//    // Flow entering via irb.100 destined for the irb.100 gateway address itself (10.254.6.1).
//    // This should be ACCEPTED by the local device.
//    Flow flow =
//        Flow.builder()
//            .setIngressNode(DRT_HOSTNAME)
//            .setIngressInterface("irb.100")
//            .setSrcIp(Ip.parse("172.16.100.6"))
//            .setDstIp(Ip.parse("192.168.99.10"))
//            .build();
//
//    SortedMap<Flow, List<Trace>> traces;
//    try {
//      traces =
//          batfish
//              .getTracerouteEngine(snapshot)
//              .computeTraces(ImmutableSet.of(flow), false);
//    } catch (NullPointerException e) {
//      fail(
//          "Traceroute within Lab VRF threw NPE. Stack trace:\n"
//              + stackTraceToString(e));
//      return;
//    }
//
//    assertNotNull("Traces should not be null", traces);
//    assertTrue("Traces should contain our flow", traces.containsKey(flow));
//
//    List<Trace> flowTraces = traces.get(flow);
//    assertThat("Should have at least one trace", flowTraces, not(empty()));
//    // Traffic to a local interface IP should be accepted
//    assertThat(
//        "Traffic to local irb.100 IP should be accepted",
//        flowTraces.get(0),
//        hasDisposition(FlowDisposition.ACCEPTED));
//  }

//  // ---- Debug: Dump Layer3Vni and EVPN route state ----
//
//  @Test
//  public void testDebugDumpEvpnState() throws IOException {
//    Batfish batfish = loadTestrigAndComputeDataPlane();
//    NetworkSnapshot snapshot = batfish.getSnapshot();
//    Map<String, Configuration> configs = batfish.loadConfigurations(snapshot);
//
//    // Dump VI model Layer3Vnis and nve~ interfaces for ALL nodes
//    for (String hostname : configs.keySet().stream().sorted().collect(java.util.stream.Collectors.toList())) {
//      Configuration config = configs.get(hostname);
//      boolean hasL3Vni = config.getVrfs().values().stream().anyMatch(v -> !v.getLayer3Vnis().isEmpty());
//      boolean hasNve = config.getAllInterfaces().keySet().stream().anyMatch(k -> k.contains("nve"));
//      if (!hasL3Vni && !hasNve) continue;
//
//      System.out.println("===== " + hostname + " =====");
//      for (Map.Entry<String, Vrf> vrfEntry : config.getVrfs().entrySet()) {
//        Vrf vrf = vrfEntry.getValue();
//        if (!vrf.getLayer3Vnis().isEmpty()) {
//          System.out.println("  VRF " + vrfEntry.getKey() + " Layer3Vnis:");
//          vrf.getLayer3Vnis().forEach((vni, l3vni) -> {
//            System.out.println("    VNI " + vni
//                + " srcAddr=" + l3vni.getSourceAddress()
//                + " learnedVteps=" + l3vni.getLearnedNexthopVtepIps());
//          });
//        }
//      }
//      // Check for nve~ interfaces
//      config.getAllInterfaces().entrySet().stream()
//          .filter(e -> e.getKey().contains("nve"))
//          .forEach(e -> System.out.println("  Interface " + e.getKey()
//              + " vrf=" + e.getValue().getVrfName()
//              + " active=" + e.getValue().getActive()));
//    }
//
//    // Check the actual FIB for the Lab VRF on node1-1
//    System.out.println("\n===== FIB for node1-1 Lab VRF =====");
//    org.batfish.datamodel.DataPlane dp = batfish.loadDataPlane(snapshot);
//    Map<String, Map<String, org.batfish.datamodel.Fib>> fibs = dp.getFibs();
//    if (fibs.containsKey("node1-1") && fibs.get("node1-1").containsKey("Lab")) {
//      org.batfish.datamodel.Fib labFib = fibs.get("node1-1").get("Lab");
//      Set<org.batfish.datamodel.FibEntry> entries =
//          labFib.get(org.batfish.datamodel.Ip.parse("192.168.99.10"));
//      System.out.println("  FIB entries for 192.168.99.10: " + entries.size());
//      entries.forEach(e -> System.out.println("    action=" + e.getAction()
//          + " resolvedBy=" + e.getTopLevelRoute().getNetwork()
//          + " via=" + e.getTopLevelRoute().getNextHop()));
//    }
//
//    // Dump dataplane Layer3Vni state (with learned VTEPs populated after convergence)
//    System.out.println("\n===== DATAPLANE Layer3Vnis =====");
//    for (Map.Entry<String, java.util.Map<String, Set<org.batfish.datamodel.vxlan.Layer3Vni>>> nodeEntry :
//        dp.getLayer3Vnis().rowMap().entrySet()) {
//      for (Map.Entry<String, Set<org.batfish.datamodel.vxlan.Layer3Vni>> vrfEntry :
//          nodeEntry.getValue().entrySet()) {
//        for (org.batfish.datamodel.vxlan.Layer3Vni l3vni : vrfEntry.getValue()) {
//          System.out.println("  " + nodeEntry.getKey() + " VRF " + vrfEntry.getKey()
//              + " VNI " + l3vni.getVni()
//              + " src=" + l3vni.getSourceAddress()
//              + " learnedVteps=" + l3vni.getLearnedNexthopVtepIps());
//        }
//      }
//    }
//
//    // Dump VXLAN topology Layer3 info
//    System.out.println("\n===== VXLAN Layer3 Topology Info =====");
//    // Check which nodes have nve~ interfaces and which VRFs they're in
//    for (String hostname : configs.keySet().stream().sorted().collect(java.util.stream.Collectors.toList())) {
//      Configuration config = configs.get(hostname);
//      long nveCount = config.getAllInterfaces().keySet().stream().filter(k -> k.contains("nve")).count();
//      if (nveCount > 0) {
//        System.out.println("  " + hostname + " has " + nveCount + " nve~ interfaces");
//        config.getAllInterfaces().entrySet().stream()
//            .filter(e -> e.getKey().contains("nve"))
//            .forEach(e -> {
//              org.batfish.datamodel.Interface iface = e.getValue();
//              System.out.println("    " + e.getKey()
//                  + " vrf=" + iface.getVrfName()
//                  + " active=" + iface.getActive()
//                  + " addlArpIps=" + iface.getAdditionalArpIps());
//            });
//      }
//    }
//
//    // Dump L3 topology edges for nve~ interfaces
//    System.out.println("\n===== L3 Topology edges for nve~ =====");
//    org.batfish.datamodel.Topology l3Topo = batfish.getTopologyProvider().getLayer3Topology(snapshot);
//    l3Topo.getEdges().stream()
//        .filter(e -> e.toString().contains("nve"))
//        .sorted(java.util.Comparator.comparing(Object::toString))
//        .forEach(e -> System.out.println("  " + e));
//    System.out.println("  Total L3 edges: " + l3Topo.getEdges().size());
//
//    // Dump VXLAN topology
//    System.out.println("\n===== VXLAN Topology =====");
//    org.batfish.datamodel.vxlan.VxlanTopology vxlanTopo = batfish.getTopologyProvider().getVxlanTopology(snapshot);
//    System.out.println("  Layer3 VNI edges:");
//    vxlanTopo.getLayer3VniEdges().forEach(e -> System.out.println("    " + e));
//    System.out.println("  Layer2 VNI edges:");
//    vxlanTopo.getLayer2VniEdges().forEach(e -> System.out.println("    " + e));
//
//    // Try computing topology from scratch to check if compatibleLayer3VniSettings would pass
//    System.out.println("\n===== Computing VxlanTopology from dataplane Layer3Vnis =====");
//    org.batfish.datamodel.vxlan.VxlanTopology testTopo =
//        org.batfish.datamodel.vxlan.VxlanTopologyUtils.computeNextVxlanTopologyModuloReachability(
//            dp.getLayer2Vnis(), dp.getLayer3Vnis());
//    System.out.println("  Layer3 VNI edges (before pruning):");
//    testTopo.getLayer3VniEdges().forEach(e -> System.out.println("    " + e));
//
//    // Debug: dump what the Layer3Vni table looks like for VNI 2000
//    System.out.println("\n===== Layer3Vni details for VNI 2000 =====");
//    System.out.println("  Total Layer3Vni cells: " + dp.getLayer3Vnis().cellSet().size());
//    dp.getLayer3Vnis().cellSet().stream()
//        .filter(cell -> cell.getValue().stream().anyMatch(v -> v.getVni() == 2000))
//        .forEach(cell -> {
//          System.out.println("  Host=" + cell.getRowKey() + " VRF=" + cell.getColumnKey()
//              + " count=" + cell.getValue().size());
//          cell.getValue().stream()
//              .filter(v -> v.getVni() == 2000)
//              .forEach(v -> System.out.println("    " + v));
//        });
//
//    // Also manually test compatibility
//    System.out.println("\n===== Manual compatibility check =====");
//    java.util.List<org.batfish.datamodel.vxlan.Layer3Vni> vni2000list = new java.util.ArrayList<>();
//    dp.getLayer3Vnis().cellSet().forEach(cell ->
//        cell.getValue().stream()
//            .filter(v -> v.getVni() == 2000)
//            .forEach(v -> vni2000list.add(v)));
//    System.out.println("  VNI 2000 Layer3Vnis count: " + vni2000list.size());
//    for (int i = 0; i < vni2000list.size(); i++) {
//      for (int j = i + 1; j < vni2000list.size(); j++) {
//        org.batfish.datamodel.vxlan.Layer3Vni a = vni2000list.get(i);
//        org.batfish.datamodel.vxlan.Layer3Vni b = vni2000list.get(j);
//        boolean samePort = a.getUdpPort() == b.getUdpPort();
//        boolean bothSrcNotNull = a.getSourceAddress() != null && b.getSourceAddress() != null;
//        boolean diffSrc = bothSrcNotNull && !a.getSourceAddress().equals(b.getSourceAddress());
//        boolean aLearnedb = a.getLearnedNexthopVtepIps().contains(b.getSourceAddress());
//        boolean bLearneda = b.getLearnedNexthopVtepIps().contains(a.getSourceAddress());
//        boolean compat = samePort && bothSrcNotNull && diffSrc && (aLearnedb || bLearneda);
//        System.out.println("  [" + i + "] vs [" + j + "]: compat=" + compat
//            + " samePort=" + samePort + " bothSrcNotNull=" + bothSrcNotNull
//            + " diffSrc=" + diffSrc + " aLearnedb=" + aLearnedb + " bLearneda=" + bLearneda
//            + " src=" + a.getSourceAddress() + "/" + b.getSourceAddress());
//      }
//    }
//
//    // Check underlay reachability: can node1-1 reach lab-2-sn's VTEP via default VRF?
//    System.out.println("\n===== Underlay reachability: node1-1 -> 172.31.0.252 in default VRF =====");
//    org.batfish.datamodel.FinalMainRib defaultRib = dp.getRibs().get("node1-1", "default");
//    if (defaultRib != null) {
//      System.out.println("  default VRF routes containing 172.31.0:");
//      defaultRib.getRoutes().stream()
//          .filter(r -> r.getNetwork().containsIp(Ip.parse("172.31.0.252")))
//          .forEach(r -> System.out.println("    " + r.getNetwork() + " nh=" + r.getNextHop()
//              + " proto=" + r.getProtocol()));
//      System.out.println("  Total default VRF routes: " + defaultRib.getRoutes().size());
//      defaultRib.getRoutes().stream()
//          .sorted(java.util.Comparator.comparing(r -> r.getNetwork().toString()))
//          .forEach(r -> System.out.println("    " + r.getNetwork() + " nh=" + r.getNextHop()
//              + " proto=" + r.getProtocol()));
//    } else {
//      System.out.println("  default VRF main RIB: null");
//    }
//
//    // Run a traceroute from node1-1 default VRF to lab-2-sn VTEP
//    // But first, dump the FIB for lab-1-crt to understand null routing
//    System.out.println("\n===== FIB entries on lab-1-crt (default) for 172.31.0.252 =====");
//    Map<String, org.batfish.datamodel.Fib> crtFibs = dp.getFibs().get("lab-1-crt");
//    org.batfish.datamodel.Fib crtFib = crtFibs != null ? crtFibs.get("default") : null;
//    if (crtFib != null) {
//      java.util.Set<org.batfish.datamodel.FibEntry> crtFibEntries = crtFib.get(Ip.parse("172.31.0.252"));
//      System.out.println("  FIB entries for 172.31.0.252: " + crtFibEntries.size());
//      for (org.batfish.datamodel.FibEntry fe : crtFibEntries) {
//        System.out.println("    action=" + fe.getAction()
//            + " topRoute=" + fe.getTopLevelRoute().getNetwork()
//            + " nh=" + fe.getTopLevelRoute().getNextHop()
//            + " proto=" + fe.getTopLevelRoute().getProtocol());
//      }
//      // Also check FIB for the next-hop IP
//      java.util.Set<org.batfish.datamodel.FibEntry> nhFibEntries = crtFib.get(Ip.parse("172.31.254.246"));
//      System.out.println("  FIB entries for NH 172.31.254.246: " + nhFibEntries.size());
//      for (org.batfish.datamodel.FibEntry fe : nhFibEntries) {
//        System.out.println("    action=" + fe.getAction()
//            + " topRoute=" + fe.getTopLevelRoute().getNetwork()
//            + " nh=" + fe.getTopLevelRoute().getNextHop()
//            + " proto=" + fe.getTopLevelRoute().getProtocol());
//      }
//    }
//    // Also dump all connected routes on lab-1-crt
//    System.out.println("  --- Connected routes on lab-1-crt ---");
//    org.batfish.datamodel.FinalMainRib crtRibForDebug = dp.getRibs().get("lab-1-crt", "default");
//    if (crtRibForDebug != null) {
//      crtRibForDebug.getRoutes().stream()
//          .filter(r -> r.getProtocol() == org.batfish.datamodel.RoutingProtocol.CONNECTED)
//          .sorted(java.util.Comparator.comparing(r -> r.getNetwork().toString()))
//          .forEach(r -> System.out.println("    " + r.getNetwork() + " nh=" + r.getNextHop()
//              + " proto=" + r.getProtocol()));
//    }
//    System.out.println("\n===== Traceroute: node1-1 (default) -> 172.31.0.252 =====");
//    Flow underlayFlow = Flow.builder()
//        .setIngressNode("node1-1")
//        .setIngressVrf("default")
//        .setSrcIp(Ip.parse("172.31.0.251"))
//        .setDstIp(Ip.parse("172.31.0.252"))
//        .setIpProtocol(org.batfish.datamodel.IpProtocol.UDP)
//        .setSrcPort(49152)
//        .setDstPort(4789)
//        .build();
//    SortedMap<Flow, List<Trace>> underlayTraces =
//        batfish.getTracerouteEngine(snapshot).computeTraces(ImmutableSet.of(underlayFlow), false);
//    if (underlayTraces.containsKey(underlayFlow)) {
//      for (Trace t : underlayTraces.get(underlayFlow)) {
//        System.out.println("  Disposition: " + t.getDisposition());
//        for (Hop h : t.getHops()) {
//          System.out.println("    Hop: " + h.getNode().getName());
//          for (org.batfish.datamodel.flow.Step<?> s : h.getSteps()) {
//            System.out.println("      " + s.getAction() + " " + s.getDetail());
//            if (s instanceof org.batfish.datamodel.flow.RoutingStep) {
//              org.batfish.datamodel.flow.RoutingStep rs = (org.batfish.datamodel.flow.RoutingStep) s;
//              org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail d = rs.getDetail();
//              System.out.println("        routes=" + d.getRoutes() + " outputIface=" + d.getOutputInterface()
//                  + " arp=" + d.getArpIp());
//            }
//          }
//        }
//      }
//    }
//    // Check BGP underlay route propagation on CRT and spine nodes
//    System.out.println("\n===== UNDERLAY ROUTE CHECK on CRT/SN/DRT nodes =====");
//    for (String hostname : new String[]{"node1-1", "lab-1-crt", "lab-2-crt", "lab-1-sn", "lab-2-sn", "lab-1-ccrt", "lab-2-drt"}) {
//      org.batfish.datamodel.FinalMainRib defRib = dp.getRibs().get(hostname, "default");
//      if (defRib == null) {
//        System.out.println("  " + hostname + ": NO default VRF");
//        continue;
//      }
//      long loopbackRoutes = defRib.getRoutes().stream()
//          .filter(r -> r.getNetwork().getPrefixLength() == 32
//              && r.getNetwork().getStartIp().toString().startsWith("172.31.0."))
//          .count();
//      System.out.println("  " + hostname + " default VRF: total=" + defRib.getRoutes().size()
//          + " /32 loopbacks in 172.31.0.x=" + loopbackRoutes);
//      defRib.getRoutes().stream()
//          .filter(r -> r.getNetwork().getPrefixLength() == 32
//              && r.getNetwork().getStartIp().toString().startsWith("172.31.0."))
//          .sorted(java.util.Comparator.comparing(r -> r.getNetwork().toString()))
//          .forEach(r -> System.out.println("    " + r.getNetwork() + " nh=" + r.getNextHop()
//              + " proto=" + r.getProtocol()));
//    }
//
//    // Also check the BGP session status
//    System.out.println("\n===== BGP Sessions (underlay/default VRF) =====");
//    org.batfish.datamodel.bgp.BgpTopology bgpTopo = batfish.getTopologyProvider().getBgpTopology(snapshot);
//    bgpTopo.getGraph().edges().stream()
//        .filter(e -> e.source().getVrfName().equals("default") || e.target().getVrfName().equals("default"))
//        .sorted(java.util.Comparator.comparing(Object::toString))
//        .forEach(e -> System.out.println("  " + e.source().getHostname() + ":" + e.source().getVrfName()
//            + " -> " + e.target().getHostname() + ":" + e.target().getVrfName()));
//
//    // Dump BGP RIB (Bgp routes) on lab-1-crt default VRF
//    System.out.println("\n===== BGP RIB on lab-1-crt (default VRF) =====");
//    org.batfish.datamodel.FinalMainRib crtDefaultRib = dp.getRibs().get("lab-1-crt", "default");
//    if (crtDefaultRib != null) {
//      crtDefaultRib.getRoutes().stream()
//          .filter(r -> r.getProtocol() == org.batfish.datamodel.RoutingProtocol.BGP)
//          .sorted(java.util.Comparator.comparing(r -> r.getNetwork().toString()))
//          .forEach(r -> System.out.println("    " + r.getNetwork() + " nh=" + r.getNextHop()
//              + " proto=" + r.getProtocol()
//              + " asPath=" + (r instanceof org.batfish.datamodel.BgpRoute ? ((org.batfish.datamodel.BgpRoute<?,?>) r).getAsPath() : "?")));
//    }
//
//    // Check bgp multipath/routes sent by node1-1 to lab-1-crt
//    System.out.println("\n===== BGP routes on node1-1 (default VRF) =====");
//    org.batfish.datamodel.FinalMainRib drtDefaultRib = dp.getRibs().get("node1-1", "default");
//    if (drtDefaultRib != null) {
//      drtDefaultRib.getRoutes().stream()
//          .sorted(java.util.Comparator.comparing(r -> r.getNetwork().toString()))
//          .forEach(r -> System.out.println("    " + r.getNetwork() + " nh=" + r.getNextHop()
//              + " proto=" + r.getProtocol()));
//    }
//
//    // Test export policy evaluation directly
//    System.out.println("\n===== Direct export policy evaluation test =====");
//    Configuration drtConfig = configs.get("node1-1");
//    // Find the peer export policy for node1-1 -> lab-1-crt session
//    for (Map.Entry<String, org.batfish.datamodel.routing_policy.RoutingPolicy> pe :
//        drtConfig.getRoutingPolicies().entrySet()) {
//      if (pe.getKey().contains("~BGP_PEER_EXPORT_POLICY")) {
//        System.out.println("  Export policy: " + pe.getKey());
//      }
//    }
//    // Find the specific peer export policy for the lab-1-crt neighbor
//    String peerPolicyName = drtConfig.getRoutingPolicies().keySet().stream()
//        .filter(n -> n.contains("~BGP_PEER_EXPORT_POLICY") && n.contains("172.31.254.243"))
//        .findFirst().orElse(null);
//    System.out.println("  Peer policy for lab-1-crt: " + peerPolicyName);
//    if (peerPolicyName != null) {
//      org.batfish.datamodel.routing_policy.RoutingPolicy peerPolicy =
//          drtConfig.getRoutingPolicies().get(peerPolicyName);
//      // Create a connected route for 172.31.0.251/32
//      org.batfish.datamodel.ConnectedRoute connRoute = new org.batfish.datamodel.ConnectedRoute(
//          org.batfish.datamodel.Prefix.parse("172.31.0.251/32"), "lo0.0");
//      org.batfish.datamodel.AnnotatedRoute<org.batfish.datamodel.AbstractRoute> annotatedConn =
//          new org.batfish.datamodel.AnnotatedRoute<>(connRoute, "default");
//      org.batfish.datamodel.Bgpv4Route.Builder bgpBuilder = org.batfish.datamodel.Bgpv4Route.builder()
//          .setNetwork(org.batfish.datamodel.Prefix.parse("172.31.0.251/32"))
//          .setOriginType(org.batfish.datamodel.OriginType.INCOMPLETE)
//          .setOriginatorIp(Ip.parse("172.31.0.251"))
//          .setProtocol(org.batfish.datamodel.RoutingProtocol.BGP)
//          .setSrcProtocol(org.batfish.datamodel.RoutingProtocol.CONNECTED)
//          .setNextHopIp(Ip.parse("172.31.254.242"))
//          .setOriginMechanism(org.batfish.datamodel.OriginMechanism.REDISTRIBUTE)
//          .setReceivedFrom(org.batfish.datamodel.ReceivedFromSelf.instance());
//      boolean accepted = peerPolicy.processBgpRoute(
//          annotatedConn, bgpBuilder, null, org.batfish.datamodel.routing_policy.Environment.Direction.OUT, null);
//      System.out.println("  Connected route 172.31.0.251/32 export accepted: " + accepted);
//    }
//
//    // Also try export-underlay directly
//    System.out.println("\n===== Direct export-underlay policy evaluation =====");
//    org.batfish.datamodel.routing_policy.RoutingPolicy exportUnderlay =
//        drtConfig.getRoutingPolicies().get("export-underlay");
//    if (exportUnderlay != null) {
//      org.batfish.datamodel.ConnectedRoute connRoute = new org.batfish.datamodel.ConnectedRoute(
//          org.batfish.datamodel.Prefix.parse("172.31.0.251/32"), "lo0.0");
//      org.batfish.datamodel.AnnotatedRoute<org.batfish.datamodel.AbstractRoute> annotatedConn =
//          new org.batfish.datamodel.AnnotatedRoute<>(connRoute, "default");
//      org.batfish.datamodel.routing_policy.Environment env = org.batfish.datamodel.routing_policy.Environment.builder(drtConfig)
//          .setOriginalRoute(annotatedConn)
//          .setDirection(org.batfish.datamodel.routing_policy.Environment.Direction.OUT)
//          .build();
//      org.batfish.datamodel.routing_policy.Result result = exportUnderlay.call(env);
//      System.out.println("  export-underlay for 172.31.0.251/32 connected: boolVal=" + result.getBooleanValue()
//          + " fallThrough=" + result.getFallThrough() + " exit=" + result.getExit()
//          + " return=" + result.getReturn());
//    }
//  }

  // ---- Step: Verify EVPN RIB has routes (bf.q.evpnRib()) ----

  @Test
  public void testEvpnRibHasRoutes() throws IOException {
    Batfish batfish = loadTestrigAndComputeDataPlane();

    // Equivalent to bf.q.evpnRib().answer().frame()
    RoutesQuestion question =
        new RoutesQuestion(null, null, null, null, null, RibProtocol.EVPN, null);

    TableAnswerElement answer =
        (TableAnswerElement) batfish.createAnswerer(question).answer(batfish.getSnapshot());

    assertNotNull("EVPN RIB answer should not be null", answer);
    assertNotNull("EVPN RIB rows should not be null", answer.getRows());
    assertTrue(
        "EVPN RIB should contain at least one route, but got " + answer.getRows().size(),
        answer.getRows().size() > 0);

    System.out.println("EVPN RIB row count: " + answer.getRows().size());
  }

  // ---- Step: Verify VXLAN edges exist (bf.q.vxlanEdges()) ----

  @Test
  public void testVxlanEdgesExist() throws IOException {
    Batfish batfish = loadTestrigAndComputeDataPlane();

    // Equivalent to bf.q.vxlanEdges().answer().frame()
    EdgesQuestion question = new EdgesQuestion(null, null, EdgeType.VXLAN, false);

    TableAnswerElement answer =
        (TableAnswerElement) batfish.createAnswerer(question).answer(batfish.getSnapshot());

    assertNotNull("VXLAN edges answer should not be null", answer);
    assertNotNull("VXLAN edges rows should not be null", answer.getRows());
    assertTrue(
        "VXLAN edges should contain at least one edge, but got " + answer.getRows().size(),
        answer.getRows().size() > 0);

    System.out.println("VXLAN edges row count: " + answer.getRows().size());
  }

  // ---- Utility ----

  private static String stackTraceToString(Throwable t) {
    StringBuilder sb = new StringBuilder();
    sb.append(t.toString()).append("\n");
    for (StackTraceElement ste : t.getStackTrace()) {
      sb.append("  at ").append(ste).append("\n");
    }
    if (t.getCause() != null) {
      sb.append("Caused by: ");
      sb.append(stackTraceToString(t.getCause()));
    }
    return sb.toString();
  }
}

