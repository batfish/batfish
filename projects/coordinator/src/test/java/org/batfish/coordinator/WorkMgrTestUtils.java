package org.batfish.coordinator;

import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.ZipUtility;
import org.batfish.coordinator.id.FileBasedIdManager;
import org.batfish.coordinator.id.IdManager;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.pojo.Topology;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.role.NodeRolesData;
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

  public static void initSnapshotWithTopology(String network, String snapshot, Set<String> nodes)
      throws IOException {
    IdManager idManager = Main.getWorkMgr().getIdManager();
    NetworkId networkId = idManager.getNetworkId(network);
    SnapshotId snapshotId =
        idManager.hasSnapshotId(snapshot, networkId)
            ? idManager.getSnapshotId(snapshot, networkId)
            : idManager.generateSnapshotId();
    idManager.assignSnapshot(snapshot, networkId, snapshotId);
    SnapshotMetadataMgr.writeMetadata(
        new SnapshotMetadata(new Date().toInstant(), null), networkId, snapshotId);
    Topology pojoTopology = new Topology(snapshot);
    pojoTopology.setNodes(nodes.stream().map(Node::new).collect(Collectors.toSet()));
    Main.getWorkMgr().getStorage().storePojoTopology(pojoTopology, networkId, snapshotId);
    org.batfish.datamodel.Topology envTopology =
        new org.batfish.datamodel.Topology(ImmutableSortedSet.of());
    Main.getWorkMgr().getStorage().storeTopology(envTopology, networkId, snapshotId);
  }

  public static void setSnapshotNodeRoles(
      NodeRolesData nodeRolesData, String network, String snapshot) throws IOException {
    IdManager idManager = Main.getWorkMgr().getIdManager();
    NetworkId networkId = idManager.getNetworkId(network);
    SnapshotId snapshotId = idManager.getSnapshotId(snapshot, networkId);
    NodeRolesId snapshotNodeRolesId = idManager.getSnapshotNodeRolesId(networkId, snapshotId);
    Main.getWorkMgr().getStorage().storeNodeRoles(nodeRolesData, snapshotNodeRolesId);
  }

  public static void initWorkManager(IdManager idManager, StorageProvider storage) {
    BatfishLogger logger = new BatfishLogger("debug", false);
    Main.mainInit(new String[] {});
    Main.setLogger(logger);
    Main.initAuthorizer();
    Main.setWorkMgr(new WorkMgr(Main.getSettings(), logger, idManager, storage));
  }

  public static void uploadTestSnapshot(String network, String snapshot, TemporaryFolder folder)
      throws IOException {
    uploadTestSnapshot(network, snapshot, "c1", folder);
  }

  public static void uploadTestSnapshot(
      String network, String snapshot, String fileName, TemporaryFolder folder) throws IOException {
    uploadTestSnapshot(network, snapshot, fileName, "content", folder);
  }

  public static void uploadTestSnapshot(
      String network, String snapshot, String fileName, String content, TemporaryFolder folder)
      throws IOException {
    Path tmpSnapshotZip = createSnapshotZip(snapshot, fileName, content, folder);
    try (InputStream inputStream = Files.newInputStream(tmpSnapshotZip)) {
      Main.getWorkMgr().uploadSnapshot(network, snapshot, inputStream, false);
    }
  }

  /** Creates and returns path to a snapshot dir with the specified config */
  public static Path createSnapshot(
      String snapshot, String fileName, String content, TemporaryFolder folder) {
    Path tmpSnapshotSrcDir = folder.getRoot().toPath().resolve(snapshot);
    // intentional duplication of snapshot to provide subdir
    Path tmpSnapshotConfig =
        tmpSnapshotSrcDir
            .resolve(snapshot)
            .resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR)
            .resolve(fileName);
    tmpSnapshotConfig.getParent().toFile().mkdirs();
    CommonUtil.writeFile(tmpSnapshotConfig, content);
    return tmpSnapshotSrcDir;
  }

  /** Creates a snapshot zip with the specified config and returns the path to that zip */
  public static Path createSnapshotZip(
      String snapshot, String fileName, String content, TemporaryFolder folder) {
    Path tmpSnapshotSrcDir = createSnapshot(snapshot, fileName, content, folder);

    Path tmpSnapshotZip = tmpSnapshotSrcDir.resolve(String.format("%s.zip", snapshot));
    ZipUtility.zipFiles(tmpSnapshotSrcDir.resolve(snapshot), tmpSnapshotZip);
    return tmpSnapshotZip;
  }
}
