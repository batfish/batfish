package tools.stress_tests;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.math.Stats.meanOf;
import static org.batfish.main.TestrigText.loadTestrig;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
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
import java.util.stream.Stream;
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
import org.batfish.symbolic.state.NeighborUnreachable;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.StateExpr;

public class SnapshotBddStressTests {
  private Batfish _batfish;
  private SortedMap<String, Configuration> _configs;

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
    _configs = _batfish.loadConfigurations(snapshot);

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
    _batfish.computeDataPlane(snapshot);
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
      long t = System.currentTimeMillis();
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
    _batfish.computeDataPlane(snapshot);
    DataPlane dataPlane = _batfish.loadDataPlane(snapshot);
    ForwardingAnalysis forwardingAnalysis = dataPlane.getForwardingAnalysis();
    IpsRoutedOutInterfacesFactory ipsRoutedOutInterfacesFactory =
        new IpsRoutedOutInterfacesFactory(dataPlane.getFibs());
    SpecifierContext ctx = _batfish.specifierContext(snapshot);
    IpSpaceAssignment ipSpaceAssignment =
        InferFromLocationIpSpaceAssignmentSpecifier.INSTANCE.resolve(
            LocationSpecifier.ALL_LOCATIONS.resolve(ctx), ctx);

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

    int warmupIters = 50;
    int measureIters = 20;
    int totalIters = warmupIters + measureIters;

    List<Long> graphTimes = new ArrayList<>();
    List<Long> successTimes = new ArrayList<>();
    List<Long> multipathTimes = new ArrayList<>();
    for (int i = 0; i < totalIters; i++) {
      System.out.println(String.format("Iter %s of %s", i, totalIters));

      BDDPacket pkt = new BDDPacket();
      long t = System.currentTimeMillis();
      Table<StateExpr, StateExpr, Transition> edgeTable =
          new BDDReachabilityAnalysisFactory(
                  pkt, _configs, forwardingAnalysis, ipsRoutedOutInterfacesFactory, false, false)
              .bddReachabilityAnalysis(ipSpaceAssignment, true)
              .getForwardEdgeTable();

      Set<StateExpr> sourceStates =
          edgeTable.rowKeySet().stream()
              .filter(OriginateInterfaceLink.class::isInstance)
              .collect(Collectors.toSet());
      Set<StateExpr> statesToKeep =
          Stream.of(successStates, failureStates, sourceStates)
              .flatMap(Set::stream)
              .collect(Collectors.toSet());

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

      t = System.currentTimeMillis();
      Map<StateExpr, BDD> success = new HashMap<>();
      successStates.forEach(s -> success.put(s, pkt.getFactory().one()));
      BDDReachabilityUtils.backwardFixpointTransposed(transposedEdgeTable, success);
      long successTime = System.currentTimeMillis() - t;

      Map<StateExpr, BDD> loops =
          new BDDLoopDetectionAnalysis(pkt, edges.stream(), sourceStates).detectLoopsStateExpr();
      Map<StateExpr, BDD> failure = new HashMap<>();
      failureStates.forEach(s -> success.put(s, pkt.getFactory().one()));
      BDDReachabilityUtils.backwardFixpointTransposed(transposedEdgeTable, success);

      BDD zero = pkt.getFactory().zero();
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

      System.out.println(
          String.format(
              "graph: %s\nsuccess: %s\nmultipath: %s", graphTime, successTime, multipathTime));

      if (i >= warmupIters) {
        graphTimes.add(graphTime);
        successTimes.add(successTime);
        multipathTimes.add(multipathTime);
      }
    }

    double graphTime = meanOf(graphTimes);
    double successTime = meanOf(successTimes);
    double multipathTime = meanOf(multipathTimes);
    System.out.println("--------- Average times (ms) -----------");
    System.out.println(
        String.format(
            "graph: %s\nsuccess: %s\nmultipath: %s", graphTime, successTime, multipathTime));
  }

  public static void main(String[] args) throws IOException, ParseException {
    String snapshotDir = args[0];
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
