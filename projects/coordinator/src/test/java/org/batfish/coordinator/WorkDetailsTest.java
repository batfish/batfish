package org.batfish.coordinator;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.coordinator.WorkDetails.WorkType;
import org.junit.Test;

/** Tests for {@link WorkQueueMgr}. */
public class WorkDetailsTest {

  @Test
  public void isOverlappingInputMatchingBases() {
    WorkDetails details1 = new WorkDetails("t1", "e1", WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t1", "e1", WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(true));
  }

  @Test
  public void isOverlappingInputMatchingBaseDelta() {
    WorkDetails details1 = new WorkDetails("t1", "e1", WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t2", "e2", "t1", "e1", true, WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(true));
  }

  @Test
  public void isOverlappingInputMatchingDeltaBase() {
    WorkDetails details1 = new WorkDetails("t1", "e1", "t2", "e2", true, WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t2", "e2", WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(true));
  }

  @Test
  public void isOverlappingInputMatchingDeltas() {
    WorkDetails details1 = new WorkDetails("t1", "e1", "t2", "e2", true, WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t3", "e3", "t2", "e2", true, WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(true));
  }

  @Test
  public void isOverlappingInputNoMatchBase() {
    WorkDetails details1 = new WorkDetails("t1", "e1", WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t3", "e3", WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(false));
  }

  @Test
  public void isOverlappingInputNoMatchDelta() {
    WorkDetails details1 = new WorkDetails("t1", "e1", "t2", "e2", true, WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t3", "e3", WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(false));
  }

  @Test
  public void isOverlappingInputNoMatchBaseDelta() {
    WorkDetails details1 = new WorkDetails("t1", "e1", "t2", "e2", true, WorkType.UNKNOWN);
    WorkDetails details2 = new WorkDetails("t3", "e3", "t4", "e4", true, WorkType.UNKNOWN);
    assertThat(details1.isOverlappingInput(details2), equalTo(false));
  }
}
