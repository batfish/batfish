package org.batfish.coordinator;

// make sure that WorkQueueMgr is never called from this class directly or indirectly
// otherwise, we risk a deadlock, since WorkQueueMgr calls into this class
// currently, this invariant is ensured by never calling out anywhere

import java.io.IOException;
import java.time.Instant;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.id.IdManager;
import org.batfish.datamodel.EnvironmentMetadata;
import org.batfish.datamodel.EnvironmentMetadata.ProcessingStatus;
import org.batfish.datamodel.TestrigMetadata;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.storage.StorageProvider;

public class TestrigMetadataMgr {

  private static StorageProvider storage() {
    return Main.getWorkMgr().getStorage();
  }

  private static IdManager idm() {
    return Main.getWorkMgr().getIdManager();
  }

  public static EnvironmentMetadata getEnvironmentMetadata(
      NetworkId networkId, SnapshotId snapshotId, String envName) throws IOException {
    TestrigMetadata trMetadata = readMetadata(networkId, snapshotId);
    return trMetadata.getEnvironments().get(envName);
  }

  public static Instant getTestrigCreationTimeOrMin(NetworkId networkId, SnapshotId snapshotId) {
    try {
      return readMetadata(networkId, snapshotId).getCreationTimestamp();
    } catch (Exception e) {
      return Instant.MIN;
    }
  }

  public static String getParentSnapshot(NetworkId network, SnapshotId snapshot)
      throws IOException {
    return readMetadata(network, snapshot).getParentSnapshot();
  }

  public static synchronized void initializeEnvironment(
      NetworkId networkId, SnapshotId snapshotId, String envName) throws IOException {
    TestrigMetadata metadata = readMetadata(networkId, snapshotId);
    metadata.initializeEnvironment(envName);
    writeMetadata(metadata, networkId, snapshotId);
  }

  public static TestrigMetadata readMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(storage().loadSnapshotMetadata(networkId, snapshotId), TestrigMetadata.class);
  }

  public static void writeMetadata(TestrigMetadata metadata, String network, String snapshot)
      throws IOException {
    NetworkId networkId = idm().getNetworkId(network);
    SnapshotId snapshotId = idm().getSnapshotId(snapshot, networkId);
    writeMetadata(metadata, networkId, snapshotId);
  }

  public static synchronized void writeMetadata(
      TestrigMetadata metadata, NetworkId networkId, SnapshotId snapshotId) throws IOException {
    storage().storeSnapshotMetadata(metadata, networkId, snapshotId);
  }

  public static synchronized void updateEnvironmentStatus(
      NetworkId networkId,
      SnapshotId snapshotId,
      String envName,
      ProcessingStatus status,
      String errMessage)
      throws IOException {
    TestrigMetadata trMetadata = readMetadata(networkId, snapshotId);
    EnvironmentMetadata environmentMetadata = trMetadata.getEnvironments().get(envName);
    environmentMetadata.updateStatus(status, errMessage);
    writeMetadata(trMetadata, networkId, snapshotId);
  }
}
