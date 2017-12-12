package org.batfish.coordinator;

public class WorkDetails {

  public enum WorkType {
    PARSING,
    DATAPLANING,
    ANSWERING, // includes analyzing
    UNKNOWN
  }

  public String baseTestrig;
  public String baseEnvironment;
  public String deltaTestrig;
  public String deltaEnvironment;
  public WorkType workType;
  public boolean isDataplaneDependent;
  public boolean isDifferential;

  public WorkDetails() {
    workType = WorkType.UNKNOWN;
  }
}
