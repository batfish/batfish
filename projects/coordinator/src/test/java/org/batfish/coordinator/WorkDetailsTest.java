package org.batfish.coordinator;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.coordinator.WorkDetails.WorkType;
import org.junit.Test;

/** Tests for {@link WorkQueueMgr}. */
public class WorkDetailsTest {

  @Test
  public void isOverlappingInputMatchingBases() {
    WorkDetails details1 = new WorkDetails("t1", WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t1", WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(true));
  }

  @Test
  public void isOverlappingInputMatchingBaseDelta() {
    WorkDetails details1 = new WorkDetails("t1", WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t2", "t1", true, WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(true));
  }

  @Test
  public void isOverlappingInputMatchingDeltaBase() {
    WorkDetails details1 = new WorkDetails("t1", "t2", true, WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t2", WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(true));
  }

  @Test
  public void isOverlappingInputMatchingDeltas() {
    WorkDetails details1 = new WorkDetails("t1", "t2", true, WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t3", "t2", true, WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(true));
  }

  @Test
  public void isOverlappingInputNoMatchBase() {
    WorkDetails details1 = new WorkDetails("t1", WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t3", WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(false));
  }

  @Test
  public void isOverlappingInputNoMatchDelta() {
    WorkDetails details1 = new WorkDetails("t1", "t2", true, WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t3", WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(false));
  }

  @Test
  public void isOverlappingInputNoMatchBaseDelta() {
    WorkDetails details1 = new WorkDetails("t1", "t2", true, WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t3", "t4", true, WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(false));
  }
}
