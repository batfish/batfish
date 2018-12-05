package org.batfish.coordinator.resources;

import java.util.Date;
import java.util.UUID;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.coordinator.QueuedWork;
import org.batfish.coordinator.WorkDetails.WorkType;

/** A bean for information about completed work */
public class CompletedWorkBean {

  private Date _dateCreated;
  private Date _dateTerminated;
  private UUID _id;
  private WorkStatusCode _status;
  private WorkType _workType;

  public CompletedWorkBean(QueuedWork work) {
    _dateCreated = work.getDateCreated();
    _dateTerminated = work.getDateTerminated();
    _id = work.getId();
    _status = work.getStatus();
    _workType = work.getDetails().workType;
  }

  public Date getDateCreated() {
    return _dateCreated;
  }

  public Date getDateTerminated() {
    return _dateTerminated;
  }

  public UUID getId() {
    return _id;
  }

  public WorkStatusCode getStatus() {
    return _status;
  }

  public WorkType getWorkType() {
    return _workType;
  }
}
