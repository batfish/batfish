package org.batfish.coordinator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.WorkItem;
import org.batfish.coordinator.WorkDetails.WorkType;
import org.junit.Test;

public final class QueuedWorkTest {

  private static QueuedWork createWork(String network, String snapshot) {
    return new QueuedWork(
        new WorkItem(network, snapshot), new WorkDetails(snapshot, WorkType.UNKNOWN));
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
