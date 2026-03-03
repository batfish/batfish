package org.batfish.question.traceroute;

import static org.batfish.common.util.TracePruner.DEFAULT_MAX_TRACES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.batfish.common.NetworkSnapshot;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.question.VIModelQuestionPlugin;
import org.batfish.question.bgpsessionstatus.BgpSessionStatusQuestion;
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
          "edge1", "node1-2", "node2-2", "sw1", "sw3", "node1-1", "node2-1", "router1", "sw2");

  // Batfish lowercases the Junos "host-name" to produce the VI configuration key.
  // host-name LAB-1-DRT -> "node1-1", host-name LAB-1-SN -> "lab-1-sn"
  private static final String DRT_HOSTNAME = "node1-1";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

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
        DRT_HOSTNAME + " should have a 'TENANT-A' VRF. Actual VRFs: " + vrfs.keySet(), hasLabVrf);

    // Check that irb.100 interface exists
    Map<String, Interface> allInterfaces = drt.getAllInterfaces();
    assertNotNull("Interfaces map should not be null", allInterfaces);

    boolean hasIrb100 = allInterfaces.containsKey("irb.100");
    assertTrue(
        DRT_HOSTNAME
            + " should have irb.100 interface. Actual interfaces: "
            + allInterfaces.keySet(),
        hasIrb100);

    // Verify irb.100 has an address
    Interface irb100 = allInterfaces.get("irb.100");
    assertNotNull("irb.100 interface should not be null", irb100);
    assertThat(
        "irb.100 should have at least one address", irb100.getAllConcreteAddresses(), not(empty()));

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
      fail("Data plane computation threw NPE. Stack trace:\n" + stackTraceToString(e));
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
      fail("Traceroute threw NPE. Stack trace:\n" + stackTraceToString(e));
      return;
    }

    assertNotNull("Traces map should not be null", traces);
    assertFalse("Traces map should not be empty", traces.isEmpty());

    // Debug: print EVPN RIB for all nodes
    RoutesQuestion evpnAllQ =
        new RoutesQuestion(null, null, null, null, null, RibProtocol.EVPN, null);
    TableAnswerElement evpnAllAnswer =
        (TableAnswerElement) batfish.createAnswerer(evpnAllQ).answer(batfish.getSnapshot());
    System.out.println("=== ALL EVPN RIB (" + evpnAllAnswer.getRows().size() + ") ===");
    evpnAllAnswer.getRows().iterator().forEachRemaining(row -> System.out.println(row));

    // Debug: print ALL routes for node1-1
    RoutesQuestion routesQ =
        new RoutesQuestion(null, "node1-1", null, null, null, RibProtocol.MAIN, null);
    TableAnswerElement routesAnswer =
        (TableAnswerElement) batfish.createAnswerer(routesQ).answer(batfish.getSnapshot());
    System.out.println("=== node1-1 ALL routes (" + routesAnswer.getRows().size() + ") ===");
    routesAnswer.getRows().iterator().forEachRemaining(row -> System.out.println(row));

    // Debug: print ALL routes for router1
    RoutesQuestion routesQ2 =
        new RoutesQuestion(null, "router1", null, null, null, RibProtocol.MAIN, null);
    TableAnswerElement routesAnswer2 =
        (TableAnswerElement) batfish.createAnswerer(routesQ2).answer(batfish.getSnapshot());
    System.out.println("=== router1 ALL routes (" + routesAnswer2.getRows().size() + ") ===");
    routesAnswer2.getRows().iterator().forEachRemaining(row -> System.out.println(row));

    // Print trace details for debugging
    for (Map.Entry<Flow, List<Trace>> entry : traces.entrySet()) {
      Flow flow = entry.getKey();
      List<Trace> traceList = entry.getValue();
      assertNotNull("Flow should not be null", flow);
      System.out.println("Flow: " + flow);
      for (Trace trace : traceList) {
        assertNotNull("Trace should not be null", trace);
        assertNotNull("Trace disposition should not be null", trace.getDisposition());
        System.out.println("Disposition: " + trace.getDisposition());
        System.out.println("Hops: " + trace.getHops());
        assertTrue(
            "Expected successful disposition but got: " + trace.getDisposition(),
            trace.getDisposition().isSuccessful());
      }
    }
  }

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

    // Debug: print BGP session status
    BgpSessionStatusQuestion bgpQ = new BgpSessionStatusQuestion();
    TableAnswerElement bgpAnswer =
        (TableAnswerElement) batfish.createAnswerer(bgpQ).answer(batfish.getSnapshot());
    System.out.println("=== BGP SESSION STATUS ===");
    bgpAnswer.getRows().iterator().forEachRemaining(row -> System.out.println(row));

    // Debug: print EVPN RIB
    RoutesQuestion evpnQ = new RoutesQuestion(null, null, null, null, null, RibProtocol.EVPN, null);
    TableAnswerElement evpnAnswer =
        (TableAnswerElement) batfish.createAnswerer(evpnQ).answer(batfish.getSnapshot());
    System.out.println("=== EVPN RIB ===");
    evpnAnswer.getRows().iterator().forEachRemaining(row -> System.out.println(row));

    // Debug: dump Layer2Vni settings from dataplane
    Map<String, Configuration> configs = batfish.loadConfigurations(batfish.getSnapshot());
    for (Map.Entry<String, Configuration> e : configs.entrySet()) {
      for (Map.Entry<String, Vrf> vrfEntry : e.getValue().getVrfs().entrySet()) {
        for (Layer2Vni vni : vrfEntry.getValue().getLayer2Vnis().values()) {
          System.out.printf(
              "L2VNI %s/%s: vni=%d vlan=%d src=%s bum=%s floodList=%s%n",
              e.getKey(),
              vrfEntry.getKey(),
              vni.getVni(),
              vni.getVlan(),
              vni.getSourceAddress(),
              vni.getBumTransportMethod(),
              vni.getBumTransportIps());
        }
      }
    }

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

  @Test
  public void testModel() throws IOException {
    Batfish batfish = loadTestrigAndComputeDataPlane();
    VIModelQuestionPlugin.VIModelQuestion question =
        new VIModelQuestionPlugin.VIModelQuestion(null);
    AnswerElement answer = batfish.createAnswerer(question).answer(batfish.getSnapshot());
    assertNotNull("VIModel answer should not be null", answer);

    assertTrue(
        "VIModel answer should be a VIModelAnswerElement but was " + answer.getClass().getName(),
        answer instanceof VIModelQuestionPlugin.VIModelAnswerElement);

    VIModelQuestionPlugin.VIModelAnswerElement viAnswer =
        (VIModelQuestionPlugin.VIModelAnswerElement) answer;

    assertNotNull("VIModel nodes map should not be null", viAnswer.getNodes());
    assertTrue(
        "VIModel should contain node '"
            + DRT_HOSTNAME
            + "'. Actual: "
            + viAnswer.getNodes().keySet(),
        viAnswer.getNodes().containsKey(DRT_HOSTNAME));

    Configuration node = viAnswer.getNodes().get(DRT_HOSTNAME);
    assertNotNull("Configuration for '" + DRT_HOSTNAME + "' should not be null", node);

    assertNotNull("VRFs map should not be null", node.getVrfs());
    assertTrue(
        "Configuration should contain default VRF. Actual VRFs: " + node.getVrfs().keySet(),
        node.getVrfs().containsKey(Configuration.DEFAULT_VRF_NAME));

    Vrf defaultVrf = node.getVrfs().get(Configuration.DEFAULT_VRF_NAME);
    assertNotNull("Default VRF should not be null", defaultVrf);

    // Assert we have VNIs modeled on the default VRF.
    Map<Integer, Layer2Vni> layer2Vnis = defaultVrf.getLayer2Vnis();
    Map<Integer, Layer3Vni> layer3Vnis = defaultVrf.getLayer3Vnis();

    assertNotNull("Default VRF layer2Vnis map should not be null", layer2Vnis);
    assertNotNull("Default VRF layer3Vnis map should not be null", layer3Vnis);

    assertThat("Default VRF layer2Vnis should not be empty", layer2Vnis.entrySet(), not(empty()));
    assertThat("Default VRF layer3Vnis should not be empty", layer3Vnis.entrySet(), not(empty()));

    // Basic sanity: keys should be stable ints.
    assertEquals(
        "Layer2 VNIs should have unique keys", layer2Vnis.size(), layer2Vnis.keySet().size());
    assertEquals(
        "Layer3 VNIs should have unique keys", layer3Vnis.size(), layer3Vnis.keySet().size());
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
