package org.batfish.coordinator;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.SnapshotMetadata;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.storage.StorageProvider;
import org.batfish.storage.TestStorageProvider;
import org.junit.Test;

/** Tests for {@link SnapshotMetadataMgr}. */
public class SnapshotMetadataMgrTest {
  /**
   * Test that a reader running concurrently with many writes never encounters a missing or stale
   * snapshot metadata.
   */
  @Test
  public void testThreadSafety() throws IOException, ExecutionException, InterruptedException {
    final int writes = 10000;
    NetworkId net = new NetworkId("net");
    SnapshotId ss = new SnapshotId("ss");

    // Mock storage, to mimic a filesystem with a non-zero time between clearing the metadata
    // (deleting it) and setting the metadata (writing/renaming the new one).
    StorageProvider storage =
        new TestStorageProvider() {
          private volatile String _meta; // volatile to force new reads across threads.

          @Override
          public @Nonnull String loadSnapshotMetadata(NetworkId networkId, SnapshotId snapshotId) {
            return _meta;
          }

          @Override
          public void storeSnapshotMetadata(
              SnapshotMetadata snapshotMetadata, NetworkId networkId, SnapshotId snapshotId) {
            _meta = null;
            _meta = BatfishObjectMapper.writeStringRuntimeError(snapshotMetadata);
          }
        };

    // Initialize snapshot metadata manager with some initial metadata.
    SnapshotMetadataMgr manager = new SnapshotMetadataMgr(storage);
    manager.writeMetadata(new SnapshotMetadata(Instant.now(), null), net, ss);

    // Start a background reader thread, which will do nothing but read continuously asserting
    // invariants over the read metadata.
    final AtomicBoolean done = new AtomicBoolean(false);
    ExecutorService pollerService = Executors.newSingleThreadExecutor();
    Future<Long> loops =
        pollerService.submit(
            () -> {
              long count = 0;
              Instant last = Instant.MIN;
              while (!done.get()) {
                ++count;
                SnapshotMetadata meta = manager.readMetadata(net, ss);
                assertThat(meta, notNullValue());
                Instant cur = meta.getCreationTimestamp();
                assertThat(cur, greaterThanOrEqualTo(last));
                last = cur;
              }
              return count;
            });

    // Write continuously (in competition with the background reader).
    for (int i = 0; i < writes; ++i) {
      manager.writeMetadata(new SnapshotMetadata(Instant.now(), null), net, ss);
    }

    // Declare the test done so the reader stops.
    done.set(true);
    // 1. If there was an invariant violated in the reader thread, the loops.get() call will throw.
    // 2. The loop must have run at least once. This mainly guards against an error in the test.
    assertThat(loops.get(), greaterThanOrEqualTo(1L));

    // Shutdown cleanly.
    pollerService.awaitTermination(1, TimeUnit.SECONDS);
  }
}
