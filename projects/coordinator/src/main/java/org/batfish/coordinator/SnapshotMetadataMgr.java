package org.batfish.coordinator;

// make sure that WorkQueueMgr is never called from this class directly or indirectly
// otherwise, we risk a deadlock, since WorkQueueMgr calls into this class
// currently, this invariant is ensured by never calling out anywhere

import java.io.IOException;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.GuardedBy;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.InitializationMetadata;
import org.batfish.datamodel.InitializationMetadata.ProcessingStatus;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.storage.StorageProvider;

@ParametersAreNonnullByDefault
public final class SnapshotMetadataMgr {

  public InitializationMetadata getInitializationMetadata(
      NetworkId networkId, SnapshotId snapshotId) throws IOException {
    SnapshotMetadata trMetadata = readMetadata(networkId, snapshotId);
    return trMetadata.getInitializationMetadata();
  }

  public Instant getSnapshotCreationTimeOrMin(NetworkId networkId, SnapshotId snapshotId) {
    try {
      return readMetadata(networkId, snapshotId).getCreationTimestamp();
    } catch (Exception e) {
      return Instant.MIN;
    }
  }

  public synchronized SnapshotMetadata readMetadata(NetworkId networkId, SnapshotId snapshotId)
      throws IOException {
    return BatfishObjectMapper.mapper()
        .readValue(_storage.loadSnapshotMetadata(networkId, snapshotId), SnapshotMetadata.class);
  }

  public void updateInitializationStatus(
      NetworkId networkId,
      SnapshotId snapshotId,
      ProcessingStatus status,
      @Nullable String errMessage)
      throws IOException {
    writeMetadata(
        readMetadata(networkId, snapshotId).updateStatus(status, errMessage),
        networkId,
        snapshotId);
  }

  public synchronized void writeMetadata(
      SnapshotMetadata metadata, NetworkId networkId, SnapshotId snapshotId) throws IOException {
    _storage.storeSnapshotMetadata(metadata, networkId, snapshotId);
  }

  public SnapshotMetadataMgr(StorageProvider storage) {
    _storage = storage;
  }

  @GuardedBy("this")
  private @Nonnull StorageProvider _storage;
}
