package tools.stress_tests;

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

public class SnapshotBddStressTests {
  private final String _snapshotDir;
  private final String _bddFactory;

  private Batfish _batfish;
  private SortedMap<String, Configuration> _configs;

  SnapshotBddStressTests(String snapshotDir, String bddFactory) throws IOException {
    _snapshotDir = snapshotDir;
    _bddFactory = bddFactory;

    Path tmp = Files.createTempDirectory(this.getClass().getSimpleName());
    _batfish = BatfishTestUtils.getBatfishFromTestrigText(loadTestrig(_snapshotDir), tmp);

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

  private BDDPacket bddPacket() {
    return new BDDPacket(BDDFactory.init(_bddFactory, 1_000_000, 125_000));
  }

  void ipAccessListToBdd() {
    while (true) {
      BDDPacket pkt = bddPacket();
      long t = System.currentTimeMillis();
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
                cfg.getIpAccessLists().values().stream().forEach(ipAccessListToBdd::toBdd);
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
      BDDPacket pkt = bddPacket();
      new BDDReachabilityAnalysisFactory(
              pkt, _configs, forwardingAnalysis, ipsRoutedOutInterfacesFactory, false, false)
          .bddReachabilityAnalysis(ipSpaceAssignment, true);
    }
  }

  public static void main(String[] args) throws IOException, ParseException {
    String snapshotDir = args[0];
    String test = args[1];
    String bddFactory = args[2];

    SnapshotBddStressTests stressTest = new SnapshotBddStressTests(snapshotDir, bddFactory);

    switch (test) {
      case "ipAccessListToBdd":
        stressTest.ipAccessListToBdd();
        break;
      case "bddReachabilityAnalysisFactory":
        stressTest.bddReachabilityAnalysisFactory();
        break;
      default:
        throw new IllegalArgumentException("Unrecognized stress test: " + test);
    }
  }
}
