package org.batfish.coordinator;

public class WorkDetails {

  public enum WorkType {
    PARSING,
    DATAPLANING,
    DATAPLANE_INDEPENDENT_ANSWERING, // includes analyzing
    DATAPLANE_DEPENDENT_ANSWERING,   // includes analyzing
    UNKNOWN
  }

  public String baseTestrig;
  public String baseEnvironment;
  public String deltaTestrig;
  public String deltaEnvironment;
  public boolean isDifferential = false;
  public WorkType workType = WorkType.UNKNOWN;
}
