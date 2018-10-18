package org.batfish.coordinator;

// make sure that WorkQueueMgr is never called from this class directly or indirectly
// otherwise, we risk a deadlock, since WorkQueueMgr calls into this class
// currently, this invariant is ensured by never calling out anywhere

import java.io.IOException;
import java.time.Instant;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.coordinator.id.IdManager;
import org.batfish.datamodel.InitializationMetadata;
import org.batfish.datamodel.InitializationMetadata.ProcessingStatus;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.storage.StorageProvider;

public class SnapshotMetadataMgr {

  private static StorageProvider storage() {
    return Main.getWorkMgr().getStorage();
  }

  private static IdManager idm() {
    return Main.getWorkMgr().getIdManager();
  }

  public static InitializationMetadata getInitializationMetadata(
      NetworkId networkId, SnapshotId snapshotId) throws IOException {
    SnapshotMetadata trMetadata = readMetadata(networkId, snapshotId);
    return trMetadata.getInitializationMetadata();
  }

  public static Instant getSnapshotCreationTimeOrMin(NetworkId networkId, SnapshotId snapshotId) {
    try {
      return readMetadata(networkId, snapshotId).getCreationTimestamp();
    } catch (Exception e) {
      return Instant.MIN;
    }
  }

  public static @Nullable SnapshotId getParentSnapshotId(NetworkId network, SnapshotId snapshot)
      throws IOException {
    return readMetadata(network, snapshot).getParentSnapshotId();
  }

  public static SnapshotMetadata readMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(storage().loadSnapshotMetadata(networkId, snapshotId), SnapshotMetadata.class);
  }

  public static synchronized void updateInitializationStatus(
      NetworkId networkId, SnapshotId snapshotId, ProcessingStatus status, String errMessage)
      throws IOException {
    writeMetadata(
        readMetadata(networkId, snapshotId).updateStatus(status, errMessage),
        networkId,
        snapshotId);
  }

  public static void writeMetadata(SnapshotMetadata metadata, String network, String snapshot)
      throws IOException {
    NetworkId networkId = idm().getNetworkId(network);
    SnapshotId snapshotId = idm().getSnapshotId(snapshot, networkId);
    writeMetadata(metadata, networkId, snapshotId);
  }

  public static synchronized void writeMetadata(
      SnapshotMetadata metadata, NetworkId networkId, SnapshotId snapshotId) throws IOException {
    storage().storeSnapshotMetadata(metadata, networkId, snapshotId);
  }
}
