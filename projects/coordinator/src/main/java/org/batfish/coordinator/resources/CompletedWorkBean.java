package org.batfish.coordinator.resources;

import static com.google.common.base.Preconditions.checkArgument;

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
    checkArgument(
        work.getStatus().isTerminated(),
        "Cannot create CompletedWorkBean from work that is not terminated");
    dateCreated = work.getDateCreated();
    dateTerminated = work.getDateTerminated();
    id = work.getId();
    status = work.getStatus();
    workType = work.getDetails().workType;
  }
}
