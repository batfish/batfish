package tools.stress_tests;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.math.Stats.meanOf;
import static org.batfish.main.TestrigText.loadTestrig;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDLoopDetectionAnalysis;
import org.batfish.bddreachability.BDDReachabilityAnalysisFactory;
import org.batfish.bddreachability.BDDReachabilityUtils;
import org.batfish.bddreachability.Edge;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.specifier.InferFromLocationIpSpaceAssignmentSpecifier;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.symbolic.state.Accept;
import org.batfish.symbolic.state.DeliveredToSubnet;
import org.batfish.symbolic.state.DropAclIn;
import org.batfish.symbolic.state.DropAclOut;
import org.batfish.symbolic.state.DropNoRoute;
import org.batfish.symbolic.state.DropNullRoute;
import org.batfish.symbolic.state.ExitsNetwork;
import org.batfish.symbolic.state.InsufficientInfo;
import org.batfish.symbolic.state.InterfaceAccept;
import org.batfish.symbolic.state.InterfaceStateExpr;
import org.batfish.symbolic.state.NeighborUnreachable;
import org.batfish.symbolic.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.NodeInterfaceExitsNetwork;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.StateExpr;

public class SnapshotBddStressTests {
  private Batfish _batfish;
  private SortedMap<String, Configuration> _configs;
  private long _parseTimeMillis;

  SnapshotBddStressTests(String snapshotDir) throws IOException {
    Path tmp = Files.createTempDirectory(this.getClass().getSimpleName());
    _batfish = BatfishTestUtils.getBatfishFromTestrigText(loadTestrig(snapshotDir), tmp);

    Settings settings = _batfish.getSettings();
    settings.setDisableUnrecognized(false);
    settings.setHaltOnConvertError(false);
    settings.setHaltOnParseError(false);
    settings.setThrowOnLexerError(false);
    settings.setThrowOnParserError(false);

    NetworkSnapshot snapshot = _batfish.getSnapshot();
    System.err.println("Initializing ss");
    long t = System.currentTimeMillis();
    _configs = _batfish.loadConfigurations(snapshot);
    _parseTimeMillis = System.currentTimeMillis() - t;

    checkState(
        true || _batfish.loadParseVendorConfigurationAnswerElement(snapshot).getErrors().isEmpty(),
        "One or more configs failed to parse");
  }

  void ipAccessListToBdd() {
    while (true) {
      BDDPacket _pkt = new BDDPacket();
      long t = System.currentTimeMillis();
      Map<String, BDDSourceManager> srcMgrs = BDDSourceManager.forNetwork(_pkt, _configs);
      _configs
          .values()
          .forEach(
              cfg -> {
                IpAccessListToBddImpl ipAccessListToBdd =
                    new IpAccessListToBddImpl(
                        _pkt,
                        srcMgrs.get(cfg.getHostname()),
                        cfg.getIpAccessLists(),
                        cfg.getIpSpaces());
                cfg.getIpAccessLists().values().stream()
                    .map(ipAccessListToBdd::toBdd)
                    .forEach(BDD::free);
              });
      System.out.println(System.currentTimeMillis() - t);
    }
  }

  void bddReachabilityAnalysisFactory() {
    NetworkSnapshot snapshot = _batfish.getSnapshot();
    long t = System.currentTimeMillis();
    _batfish.computeDataPlane(snapshot);
    long dataPlaneTimeMillis = System.currentTimeMillis() - t;
    DataPlane dataPlane = _batfish.loadDataPlane(snapshot);
    ForwardingAnalysis forwardingAnalysis = dataPlane.getForwardingAnalysis();
    IpsRoutedOutInterfacesFactory ipsRoutedOutInterfacesFactory =
        new IpsRoutedOutInterfacesFactory(dataPlane.getFibs());
    SpecifierContext ctx = _batfish.specifierContext(snapshot);
    IpSpaceAssignment ipSpaceAssignment =
        InferFromLocationIpSpaceAssignmentSpecifier.INSTANCE.resolve(
            LocationSpecifier.ALL_LOCATIONS.resolve(ctx), ctx);

    while (true) {
      BDDPacket pkt = new BDDPacket();
      t = System.currentTimeMillis();
      int size =
          new BDDReachabilityAnalysisFactory(
                  pkt, _configs, forwardingAnalysis, ipsRoutedOutInterfacesFactory, false, false)
              .bddReachabilityAnalysis(ipSpaceAssignment, true)
              .getForwardEdgeTable()
              .size();
      assert size > 0;
      System.out.println(System.currentTimeMillis() - t);
    }
  }

  void multipathConsistency() {
    NetworkSnapshot snapshot = _batfish.getSnapshot();

    System.err.println("Generating dp");
    long t = System.currentTimeMillis();
    _batfish.computeDataPlane(snapshot);
    long dataPlaneTimeMillis = System.currentTimeMillis() - t;

    System.err.println("Loading dp and computing forwarding analysis, etc");
    DataPlane dataPlane = _batfish.loadDataPlane(snapshot);
    ForwardingAnalysis forwardingAnalysis = dataPlane.getForwardingAnalysis();
    IpsRoutedOutInterfacesFactory ipsRoutedOutInterfacesFactory =
        new IpsRoutedOutInterfacesFactory(dataPlane.getFibs());
    SpecifierContext ctx = _batfish.specifierContext(snapshot);
    IpSpaceAssignment ipSpaceAssignment =
        InferFromLocationIpSpaceAssignmentSpecifier.INSTANCE.resolve(
            LocationSpecifier.ALL_LOCATIONS.resolve(ctx), ctx);

    long activeL3Ifaces =
        _configs.values().stream().mapToLong(c -> c.activeL3Interfaces().count()).sum();
    long l3Edges = _batfish.getTopologyProvider().getLayer3Topology(snapshot).getEdges().size();

    Set<StateExpr> successStates =
        ImmutableSet.of(Accept.INSTANCE, DeliveredToSubnet.INSTANCE, ExitsNetwork.INSTANCE);
    Set<StateExpr> failureStates =
        ImmutableSet.of(
            InsufficientInfo.INSTANCE,
            NeighborUnreachable.INSTANCE,
            DropAclIn.INSTANCE,
            DropAclOut.INSTANCE,
            DropNoRoute.INSTANCE,
            DropNullRoute.INSTANCE);

    int actualIters = 0;

    List<Long> graphTimes = new ArrayList<>();
    List<Long> successTimes = new ArrayList<>();
    List<Long> multipathTimes = new ArrayList<>();
    List<Double> perDestTimes = new ArrayList<>();
    int numSources = 0;
    int numDestinations = 0;

    long warmupStartTime = System.currentTimeMillis();
    long minWarmupTime = 3 * 60 * 1000; // 3 minutes
    long runStartTime = Long.MAX_VALUE;
    long minRunTime = 5 * 60 * 1000; // 5 minutes

    while (actualIters < 2
        || (System.currentTimeMillis() - warmupStartTime) < minWarmupTime
        || (System.currentTimeMillis() - runStartTime) < minRunTime) {
      boolean warmup = (System.currentTimeMillis() - warmupStartTime) < minWarmupTime;
      ++actualIters;
      if (!warmup && runStartTime == Long.MAX_VALUE) {
        runStartTime = System.currentTimeMillis();
      }
      long remainingMs =
          warmup
              ? minWarmupTime - (System.currentTimeMillis() - warmupStartTime)
              : minRunTime - (System.currentTimeMillis() - runStartTime);
      System.out.printf(
          "Iter %s%s (%f remaining)%n",
          actualIters, warmup ? " (warmup)" : "", remainingMs / 1000.0);

      BDDPacket pkt = new BDDPacket();
      t = System.currentTimeMillis();
      Table<StateExpr, StateExpr, Transition> edgeTable =
          new BDDReachabilityAnalysisFactory(
                  pkt, _configs, forwardingAnalysis, ipsRoutedOutInterfacesFactory, false, false)
              .bddReachabilityAnalysis(ipSpaceAssignment, true)
              .getForwardEdgeTable();

      Set<StateExpr> sourceStates =
          edgeTable.rowKeySet().stream()
              .filter(OriginateInterfaceLink.class::isInstance)
              .collect(Collectors.toSet());
      numSources = sourceStates.size();
      Multimap<NodeInterfacePair, StateExpr> destinationStates =
          edgeTable.rowKeySet().stream()
              .filter(
                  s ->
                      s instanceof InterfaceAccept
                          || s instanceof NodeInterfaceExitsNetwork
                          || s instanceof NodeInterfaceDeliveredToSubnet)
              .collect(
                  ImmutableListMultimap.toImmutableListMultimap(
                      s -> {
                        InterfaceStateExpr is = ((InterfaceStateExpr) s);
                        NodeInterfacePair nip =
                            NodeInterfacePair.of(is.getHostname(), is.getInterface());
                        return nip;
                      },
                      s -> s));
      numDestinations = destinationStates.asMap().size();
      Set<StateExpr> statesToKeep =
          ImmutableSet.<StateExpr>builder()
              .addAll(successStates)
              .addAll(failureStates)
              .addAll(sourceStates)
              .addAll(destinationStates.values())
              .build();

      Collection<Edge> edges =
          edgeTable.cellSet().stream()
              .map(c -> new Edge(c.getRowKey(), c.getColumnKey(), c.getValue()))
              .collect(Collectors.toSet());
      edges =
          org.batfish.bddreachability.BDDReachabilityGraphOptimizer.optimize(
              edges, statesToKeep, true);
      Table<StateExpr, StateExpr, Transition> transposedEdgeTable =
          BDDReachabilityUtils.transposeAndMaterialize(edges);
      long graphTime = System.currentTimeMillis() - t;

      // Do destination reachability per interface
      t = System.currentTimeMillis();
      for (Collection<StateExpr> states : destinationStates.asMap().values()) {
        Map<StateExpr, BDD> answer = new HashMap<>();
        for (StateExpr dstState : states) {
          answer.put(dstState, pkt.getFactory().one()); // TODO: refine by header space if needed
        }
        BDDReachabilityUtils.backwardFixpointTransposed(transposedEdgeTable, answer);
        answer.values().forEach(BDD::free);
      }
      double perDstTime = (System.currentTimeMillis() - t + 0.0) / numDestinations;

      // Do multipath now
      t = System.currentTimeMillis();
      Map<StateExpr, BDD> success = new HashMap<>();
      successStates.forEach(s -> success.put(s, pkt.getFactory().one()));
      BDDReachabilityUtils.backwardFixpointTransposed(transposedEdgeTable, success);
      long successTime = System.currentTimeMillis() - t;

      Map<StateExpr, BDD> loops =
          new BDDLoopDetectionAnalysis(pkt, edges.stream(), sourceStates).detectLoopsStateExpr();
      Map<StateExpr, BDD> failure = new HashMap<>();
      failureStates.forEach(s -> failure.put(s, pkt.getFactory().one()));
      BDDReachabilityUtils.backwardFixpointTransposed(transposedEdgeTable, failure);

      // Compute the MPC answer from success and failure and loop.
      BDD zero = pkt.getFactory().zero();
      @SuppressWarnings("unused") // answer computed but not used below
      Map<StateExpr, BDD> multipath =
          success.entrySet().stream()
              .filter(e -> e.getKey() instanceof OriginateInterfaceLink)
              .map(
                  entry -> {
                    StateExpr s = entry.getKey();
                    BDD successBdd = entry.getValue();
                    BDD failureBdd = failure.getOrDefault(s, zero);
                    BDD loopBdd = loops.getOrDefault(s, zero);
                    BDD mc = successBdd.and(failureBdd.or(loopBdd));
                    return mc.isZero() ? null : Maps.immutableEntry(s, mc);
                  })
              .filter(Objects::nonNull)
              .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

      long multipathTime = System.currentTimeMillis() - t;

      System.out.printf(
          "  graph: %s\n  perDest: %s\n  totalPerDest: %s\n  success: %s\n  multipath: %s%n",
          graphTime, perDstTime, perDstTime * numDestinations, successTime, multipathTime);

      if (!warmup) {
        graphTimes.add(graphTime);
        successTimes.add(successTime);
        multipathTimes.add(multipathTime);
        perDestTimes.add(perDstTime);
      }
    }

    double graphTime = meanOf(graphTimes);
    double successTime = meanOf(successTimes);
    double multipathTime = meanOf(multipathTimes);
    double perDestTime = meanOf(perDestTimes);
    System.out.println("--------- Average times (ms) -----------");
    System.out.printf(
        "init: %s\ndataplane: %s\ngraph: %s\nperDest: %s\nsuccess: %s\nmultipath: %s\nsources: %s\ndestinations: %s\nActive L3 interfaces: %d\nL3 edges: %d\n\n",
        _parseTimeMillis,
        dataPlaneTimeMillis,
        graphTime,
        perDestTime,
        successTime,
        multipathTime,
        numSources,
        numDestinations,
        activeL3Ifaces,
        l3Edges);
  }

  public static void main(String[] args) throws IOException, ParseException {
    String snapshotDir = args[0];
    System.out.printf("net: %s\n", snapshotDir);
    String test = args[1];

    SnapshotBddStressTests stressTest = new SnapshotBddStressTests(snapshotDir);

    switch (test) {
      case "ipAccessListToBdd":
        stressTest.ipAccessListToBdd();
        break;
      case "bddReachabilityAnalysisFactory":
        stressTest.bddReachabilityAnalysisFactory();
        break;
      case "multipathConsistency":
        stressTest.multipathConsistency();
        break;
      default:
        throw new IllegalArgumentException("Unrecognized stress test: " + test);
    }
  }
}
