package org.batfish.tools.bdd;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.main.TestrigText.loadTestrig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Map;
import java.util.SortedMap;
import net.sf.javabdd.BDDFactory;
import org.batfish.bddreachability.BDDReachabilityAnalysisFactory;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory;
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

public class SnapshotBddLeakTests {
  private Batfish _batfish;
  private SortedMap<String, Configuration> _configs;

  SnapshotBddLeakTests(String snapshotDir) throws IOException {
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
        _batfish.loadParseVendorConfigurationAnswerElement(snapshot).getErrors().isEmpty(),
        "One or more configs failed to parse");
  }

  int ipAccessListToBdd() {
    BDDPacket pkt = new BDDPacket();
    BDDFactory factory = pkt.getFactory();
    factory.runGC();
    int nodes = factory.getNodeNum();
    Map<String, BDDSourceManager> srcMgrs = BDDSourceManager.forNetwork(pkt, _configs);
    _configs
        .values()
        .forEach(
            cfg -> {
              IpAccessListToBddImpl ipAccessListToBdd =
                  new IpAccessListToBddImpl(
                      pkt,
                      srcMgrs.get(cfg.getHostname()),
                      cfg.getIpAccessLists(),
                      cfg.getIpSpaces());
              cfg.getIpAccessLists().values().forEach(ipAccessListToBdd::toBdd);
            });
    factory.runGC();
    return factory.getNodeNum() - nodes;
  }

  int bddReachabilityAnalysisFactory() {
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

    BDDPacket pkt = new BDDPacket();
    BDDFactory factory = pkt.getFactory();
    factory.runGC();
    int nodes = factory.getNodeNum();
    new BDDReachabilityAnalysisFactory(
            pkt, _configs, forwardingAnalysis, ipsRoutedOutInterfacesFactory, false, false)
        .bddReachabilityAnalysis(ipSpaceAssignment, true);
    factory.runGC();
    return factory.getNodeNum() - nodes;
  }

  public static void main(String[] args) throws IOException, ParseException {
    if (args.length < 2) {
      System.out.printf(
          "Usage: %s <snapshot_dir> test_name ...\n", SnapshotBddLeakTests.class.getSimpleName());
      return;
    }

    String snapshotDir = args[0];

    SnapshotBddLeakTests test = new SnapshotBddLeakTests(snapshotDir);

    for (int i = 1; i < args.length; i++) {
      String name = args[i];
      int leakedBdds;
      switch (name) {
        case "ipAccessListToBdd":
          leakedBdds = test.ipAccessListToBdd();
          break;
        case "bddReachabilityAnalysisFactory":
          leakedBdds = test.bddReachabilityAnalysisFactory();
          break;
        default:
          throw new IllegalArgumentException("Unrecognized test name: " + name);
      }
      System.out.printf("%s: %s\n", name, leakedBdds);
    }
  }
}
