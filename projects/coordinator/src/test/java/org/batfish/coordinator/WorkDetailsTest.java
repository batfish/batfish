package org.batfish.coordinator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.coordinator.WorkDetails.WorkType;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link WorkQueueMgr}. */
public final class WorkDetailsTest {

  private WorkDetails.Builder _builder;

  @Before
  public void setup() {
    _builder =
        WorkDetails.builder().setNetworkId(new NetworkId("foo")).setWorkType(WorkType.UNKNOWN);
  }

  @Test
  public void isOverlappingInputMatchingBases() {
    WorkDetails details1 = _builder.setSnapshotId(new SnapshotId("t1")).build();
    WorkDetails details2 = _builder.build();
    assertTrue(details1.isOverlappingInput(details2));
  }

  @Test
  public void isOverlappingInputMatchingBaseDelta() {
    WorkDetails details1 = _builder.setSnapshotId(new SnapshotId("t1")).build();
    WorkDetails details2 =
        _builder
            .setSnapshotId(new SnapshotId("t2"))
            .setReferenceSnapshotId(new SnapshotId("t1"))
            .setIsDifferential(true)
            .build();
    assertTrue(details1.isOverlappingInput(details2));
  }

  @Test
  public void isOverlappingInputMatchingDeltaBase() {
    WorkDetails details1 =
        _builder
            .setSnapshotId(new SnapshotId("t1"))
            .setReferenceSnapshotId(new SnapshotId("t2"))
            .setIsDifferential(true)
            .build();
    WorkDetails details2 =
        _builder
            .setSnapshotId(new SnapshotId("t2"))
            .setReferenceSnapshotId(null)
            .setIsDifferential(false)
            .build();

    assertTrue(details1.isOverlappingInput(details2));
  }

  @Test
  public void isOverlappingInputMatchingDeltas() {
    WorkDetails details1 =
        _builder
            .setSnapshotId(new SnapshotId("t1"))
            .setReferenceSnapshotId(new SnapshotId("t2"))
            .setIsDifferential(true)
            .build();
    WorkDetails details2 =
        _builder
            .setSnapshotId(new SnapshotId("t3"))
            .setReferenceSnapshotId(new SnapshotId("t2"))
            .setIsDifferential(true)
            .build();

    assertTrue(details1.isOverlappingInput(details2));
  }

  @Test
  public void isOverlappingInputNoMatchBase() {
    WorkDetails details1 = _builder.setSnapshotId(new SnapshotId("t1")).build();
    WorkDetails details2 = _builder.setSnapshotId(new SnapshotId("t3")).build();

    assertFalse(details1.isOverlappingInput(details2));
  }

  @Test
  public void isOverlappingInputNoMatchDelta() {
    WorkDetails details1 =
        _builder
            .setSnapshotId(new SnapshotId("t1"))
            .setReferenceSnapshotId(new SnapshotId("t2"))
            .setIsDifferential(true)
            .build();
    WorkDetails details2 =
        _builder
            .setSnapshotId(new SnapshotId("t3"))
            .setReferenceSnapshotId(null)
            .setIsDifferential(false)
            .build();

    assertFalse(details1.isOverlappingInput(details2));
  }

  @Test
  public void isOverlappingInputNoMatchBaseDelta() {
    WorkDetails details1 =
        _builder
            .setSnapshotId(new SnapshotId("t1"))
            .setReferenceSnapshotId(new SnapshotId("t2"))
            .setIsDifferential(true)
            .build();
    WorkDetails details2 =
        _builder
            .setSnapshotId(new SnapshotId("t3"))
            .setReferenceSnapshotId(new SnapshotId("t4"))
            .setIsDifferential(true)
            .build();

    assertFalse(details1.isOverlappingInput(details2));
  }
}
