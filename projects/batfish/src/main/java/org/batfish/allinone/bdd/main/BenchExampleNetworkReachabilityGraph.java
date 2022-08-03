package org.batfish.allinone.bdd.main;

import static org.batfish.main.BatfishTestUtils.getBatfishFromTestrigText;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.util.SortedMap;
import jdk.jfr.Recording;
import org.batfish.bddreachability.BDDReachabilityAnalysisFactory;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.specifier.InferFromLocationIpSpaceAssignmentSpecifier;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class BenchExampleNetworkReachabilityGraph {
  @Param({"REQUIRED INPUT PARAM"})
  public String snapshotDir;

  private Batfish _batfish;
  private BDDPacket _pkt;
  private SortedMap<String, Configuration> _configs;
  private ForwardingAnalysis _forwardingAnalysis;
  private IpsRoutedOutInterfacesFactory _ipsRoutedOutInterfacesFactory;
  private IpSpaceAssignment _ipSpaceAssignment;

  private Recording _jfrRecording;

  private Batfish createBatfish() throws IOException {
    Path tmp = File.createTempFile(this.getClass().getSimpleName(), null).toPath();
    return getBatfishFromTestrigText(loadTestrig(snapshotDir), tmp);
  }

  private static String fileText(File f) throws IOException {
    return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
  }

  private static TestrigText loadTestrig(String dir) throws IOException {
    TestrigText.Builder builder = TestrigText.builder();

    Path snapshotDir = Paths.get(dir);

    // layer 1 topology
    try {
      builder.setLayer1TopologyBytes(
          Files.readAllBytes(snapshotDir.resolve("batfish/layer1_topology.json")));
      System.out.println("read layer1_topology.json");
    } catch (Exception e) {
      e.printStackTrace();
    }

    // isp config
    try {
      builder.setIspConfigBytes(Files.readAllBytes(snapshotDir.resolve("batfish/isp_config.json")));
      System.out.println("read isp_config.json");
    } catch (Exception e) {
      e.printStackTrace();
    }

    // configs
    builder.setConfigurationText(
        Arrays.stream(snapshotDir.resolve("configs").toFile().listFiles())
            .collect(
                ImmutableMap.toImmutableMap(
                    f -> f.getName(),
                    f -> {
                      try {
                        return fileText(f);
                      } catch (IOException e) {
                        throw new RuntimeException(e);
                      }
                    })));

    // hosts
    builder.setHostsBytes(
        Arrays.stream(snapshotDir.resolve("hosts").toFile().listFiles())
            .collect(
                ImmutableMap.toImmutableMap(
                    f -> f.getName(),
                    f -> {
                      try {
                        return Files.readAllBytes(f.toPath());
                      } catch (IOException e) {
                        throw new RuntimeException(e);
                      }
                    })));

    return builder.build();
  }

  @Setup(Level.Trial)
  public void setUp() throws IOException, ParseException {
    Path tmp = Files.createTempDirectory(this.getClass().getSimpleName());
    _batfish = BatfishTestUtils.getBatfishFromTestrigText(loadTestrig(snapshotDir), tmp);
    _batfish.getSettings().setHaltOnParseError(false);
    NetworkSnapshot snapshot = _batfish.getSnapshot();
    _configs = _batfish.loadConfigurations(snapshot);
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
  public void setUpInvocation() throws IOException, ParseException {
    _pkt = new BDDPacket();
    // jfr
    jdk.jfr.Configuration conf = jdk.jfr.Configuration.getConfiguration("profile");
    _jfrRecording = new Recording(conf);
  }

  @Benchmark
  public int bench() {
    _jfrRecording.start();
    int numEdges =
        new BDDReachabilityAnalysisFactory(
                _pkt, _configs, _forwardingAnalysis, _ipsRoutedOutInterfacesFactory, false, false)
            .bddReachabilityAnalysis(_ipSpaceAssignment, true)
            .getForwardEdgeTable()
            .size();
    _jfrRecording.stop();
    return numEdges;
  }

  @TearDown(Level.Invocation)
  public void teardown() throws IOException {
    File jfrFile = File.createTempFile("profile", ".jfr", new File("/tmp/bench"));
    _jfrRecording.dump(jfrFile.toPath());
  }
}
