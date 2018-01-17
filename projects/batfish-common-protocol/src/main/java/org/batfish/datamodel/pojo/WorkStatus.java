package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.common.Task;
import org.batfish.common.WorkItem;

/**
 * The {@link WorkStatus WorkStatus} is an Object representation of WorkStatus
 *
 * <p>Each {@link WorkStatus WorkStatus} contains the workitem, its workstatus, and taskstatus.
 */
public class WorkStatus {

  private static final String PROP_TASK_STATUS = "taskstatus";
  private static final String PROP_WORK_ITEM = "workitem";
  private static final String PROP_WORK_STATUS_CODE = "workstatuscode";

  private final Task _taskStatus;

  private final WorkItem _workItem;

  private final WorkStatusCode _workStatusCode;

  @JsonCreator
  public WorkStatus(
      @JsonProperty(PROP_WORK_ITEM) WorkItem wItem,
      @JsonProperty(PROP_WORK_STATUS_CODE) WorkStatusCode wStatusCode,
      @JsonProperty(PROP_TASK_STATUS) Task taskStatus) {
    _workItem = wItem;
    _workStatusCode = wStatusCode;
    _taskStatus = taskStatus;
  }

  @JsonProperty(PROP_TASK_STATUS)
  public Task getTaskStatusStr() {
    return _taskStatus;
  }

  @JsonProperty(PROP_WORK_ITEM)
  public WorkItem getWorkItem() {
    return _workItem;
  }

  @JsonProperty(PROP_WORK_STATUS_CODE)
  public WorkStatusCode getWorkStatusCode() {
    return _workStatusCode;
  }
}
