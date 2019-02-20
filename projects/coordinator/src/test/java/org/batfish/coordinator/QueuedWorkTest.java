package org.batfish.coordinator;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.WorkItem;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.junit.Test;

public final class QueuedWorkTest {

  private static QueuedWork createWork(String network, String snapshot) {
    return new QueuedWork(
        new WorkItem(network, snapshot),
        WorkDetails.builder()
            .setWorkType(WorkType.UNKNOWN)
            .setNetworkId(new NetworkId(network + "-ID"))
            .setSnapshotId(new SnapshotId(snapshot + "-ID"))
            .build());
  }

  @Test
  public void testDateTerminated() {
    QueuedWork work = createWork("network", "snapshot");
    // Work should start with no dateTerminated
    assertThat(work._dateTerminated, is(nullValue()));

    // Work should still have no dateTerminated with some other non-terminated status
    work.setStatus(WorkStatusCode.TRYINGTOASSIGN);
    assertThat(work._dateTerminated, is(nullValue()));

    // Work should have dateTerminated after being terminated
    work.setStatus(WorkStatusCode.TERMINATEDNORMALLY);
    assertThat(work._dateTerminated, not(is(nullValue())));
  }
}
