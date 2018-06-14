package org.batfish.coordinator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.config.Settings;
import org.batfish.datamodel.TestrigMetadata;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.pojo.Topology;
import org.junit.rules.TemporaryFolder;

public final class WorkMgrTestUtils {

  private WorkMgrTestUtils() {}

  public static void initWorkManager(TemporaryFolder folder) throws Exception {
    BatfishLogger logger = new BatfishLogger("debug", false);
    Settings settings = new Settings(new String[] {});
    Main.mainInit(new String[] {"-containerslocation", folder.getRoot().toString()});
    Main.setLogger(logger);
    Main.initAuthorizer();
    Main.setWorkMgr(new WorkMgr(settings, logger));
  }

  public static void initTestrigWithTopology(String container, String testrig, Set<String> nodes)
      throws IOException {
    Path containerDir =
        Main.getSettings().getContainersLocation().resolve(container).toAbsolutePath();
    Files.createDirectories(containerDir.resolve(BfConsts.RELPATH_TESTRIGS_DIR).resolve(testrig));
    TestrigMetadataMgr.writeMetadata(
        new TestrigMetadata(new Date().toInstant(), "env"), container, testrig);
    Topology topology = new Topology(testrig);
    topology.setNodes(nodes.stream().map(n -> new Node(n)).collect(Collectors.toSet()));
    CommonUtil.writeFile(
        Main.getWorkMgr()
            .getdirTestrig(container, testrig)
            .resolve(BfConsts.RELPATH_TESTRIG_POJO_TOPOLOGY_PATH),
        BatfishObjectMapper.mapper().writeValueAsString(topology));
  }
}
