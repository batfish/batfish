package org.batfish.coordinator.resources;

import java.util.Date;
import java.util.UUID;
import org.batfish.common.CoordConsts.WorkStatusCode;
import org.batfish.coordinator.QueuedWork;
import org.batfish.coordinator.WorkDetails.WorkType;

/** A bean for information about work */
public class WorkBean {

  public final Date dateCreated;
  public final Date dateTerminated;
  public final UUID id;
  public final WorkStatusCode status;
  public final WorkType workType;

  public WorkBean(QueuedWork work) {
    dateCreated = work.getDateCreated();
    dateTerminated = work.getDateTerminated();
    id = work.getId();
    status = work.getStatus();
    workType = work.getDetails().getWorkType();
  }
}
