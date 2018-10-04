package org.batfish.coordinator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.coordinator.id.FileBasedIdManager;
import org.batfish.coordinator.id.IdManager;
import org.batfish.datamodel.TestrigMetadata;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.pojo.Topology;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.storage.FileBasedStorage;
import org.batfish.storage.StorageProvider;
import org.junit.rules.TemporaryFolder;

public final class WorkMgrTestUtils {

  private WorkMgrTestUtils() {}

  public static void initWorkManager(TemporaryFolder folder) throws Exception {
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {"-containerslocation", folder.getRoot().toString()});
    Main.setLogger(logger);
    Main.initAuthorizer();
    Main.setWorkMgr(
        new WorkMgr(
            Main.getSettings(),
            logger,
            new FileBasedIdManager(Main.getSettings().getContainersLocation()),
            new FileBasedStorage(Main.getSettings().getContainersLocation(), logger)));
  }

  public static void initTestrigWithTopology(String container, String testrig, Set<String> nodes)
      throws IOException {
    IdManager idManager = Main.getWorkMgr().getIdManager();
    NetworkId networkId = idManager.getNetworkId(container);
    SnapshotId snapshotId =
        idManager.hasSnapshotId(testrig, networkId)
            ? idManager.getSnapshotId(testrig, networkId)
            : idManager.generateSnapshotId();
    idManager.assignSnapshot(testrig, networkId, snapshotId);
    TestrigMetadataMgr.writeMetadata(
        new TestrigMetadata(new Date().toInstant(), "env", null), networkId, snapshotId);
    Topology topology = new Topology(testrig);
    topology.setNodes(nodes.stream().map(n -> new Node(n)).collect(Collectors.toSet()));
    Path outputDir =
        Main.getWorkMgr().getdirSnapshot(container, testrig).resolve(BfConsts.RELPATH_OUTPUT);
    if (!outputDir.toFile().exists() && !outputDir.toFile().mkdirs()) {
      throw new IOException(String.format("Unable to create directory %s", outputDir));
    }
    CommonUtil.writeFile(
        outputDir.resolve(BfConsts.RELPATH_TESTRIG_POJO_TOPOLOGY_PATH),
        BatfishObjectMapper.mapper().writeValueAsString(topology));
  }

  public static void initWorkManager(IdManager idManager, StorageProvider storage) {
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {});
    Main.setLogger(logger);
    Main.initAuthorizer();
    Main.setWorkMgr(new WorkMgr(Main.getSettings(), logger, idManager, storage));
  }
}
