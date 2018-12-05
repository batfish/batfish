package org.batfish.coordinator.resources;

import java.util.Date;
import java.util.UUID;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.coordinator.QueuedWork;
import org.batfish.coordinator.WorkDetails.WorkType;

/** A bean for information about completed work */
public class CompletedWorkBean {

  public Date dateCreated;
  public Date dateTerminated;
  public UUID id;
  public WorkStatusCode status;
  public WorkType workType;

  public CompletedWorkBean(QueuedWork work) {
    status = work.getStatus();
    if (status.isTerminated()) {
      throw new IllegalArgumentException(
          String.format("Cannot create CompletedWorkBean from work with status %s", status));
    }
    dateCreated = work.getDateCreated();
    dateTerminated = work.getDateTerminated();
    id = work.getId();
    workType = work.getDetails().workType;
  }
}
