package tools.benchmarks;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.main.TestrigText.loadTestrig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import net.sf.javabdd.BDD;
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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SnapshotBddBenchmarks {
  @Param({"REQUIRED INPUT PARAM"})
  public String snapshotDir;

  private Batfish _batfish;
  private BDDPacket _pkt;
  private SortedMap<String, Configuration> _configs;
  private ForwardingAnalysis _forwardingAnalysis;
  private IpsRoutedOutInterfacesFactory _ipsRoutedOutInterfacesFactory;
  private IpSpaceAssignment _ipSpaceAssignment;

  @Setup(Level.Trial)
  public void setUp() throws IOException, ParseException {
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

    checkState(!_configs.isEmpty(), "No configs were parsed");

    _batfish.computeDataPlane(snapshot);
    DataPlane dataPlane = _batfish.loadDataPlane(snapshot);
    _forwardingAnalysis = dataPlane.getForwardingAnalysis();
    _ipsRoutedOutInterfacesFactory = new IpsRoutedOutInterfacesFactory(dataPlane.getFibs());
    SpecifierContext ctx = _batfish.specifierContext(snapshot);
    _ipSpaceAssignment =
        InferFromLocationIpSpaceAssignmentSpecifier.INSTANCE.resolve(
            LocationSpecifier.ALL_LOCATIONS.resolve(ctx), ctx);
  }

  @Setup(Level.Invocation)
  public void setupInvocation() {
    _pkt = new BDDPacket();
  }

  @Benchmark
  public int bddReachabilityAnalysisFactory() {
    return new BDDReachabilityAnalysisFactory(
            _pkt, _configs, _forwardingAnalysis, _ipsRoutedOutInterfacesFactory, false, false)
        .bddReachabilityAnalysis(_ipSpaceAssignment, true)
        .getForwardEdgeTable()
        .size();
  }

  @Benchmark
  public long ipAccessListToBdd() {
    Map<String, BDDSourceManager> srcMgrs = BDDSourceManager.forNetwork(_pkt, _configs);
    return _configs.entrySet().stream()
        .mapToLong(
            entry -> {
              String hostname = entry.getKey();
              Configuration cfg = entry.getValue();
              IpAccessListToBddImpl ipAccessListToBdd =
                  new IpAccessListToBddImpl(
                      _pkt, srcMgrs.get(hostname), cfg.getIpAccessLists(), cfg.getIpSpaces());
              return cfg.getIpAccessLists().values().stream()
                  .map(ipAccessListToBdd::toBdd)
                  .peek(BDD::free)
                  .count();
            })
        .sum();
  }
}
