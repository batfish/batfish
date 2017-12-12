package org.batfish.coordinator;

public class WorkDetails {

  public enum WorkType {
    PARSING,
    DATAPLANING,
    DATAPLANE_INDEPENDENT_ANSWERING, // includes analyzing
    DATAPLANE_DEPENDENT_ANSWERING, // includes analyzing
    UNKNOWN
  }

  public String baseTestrig;
  public String baseEnvironment;
  public String deltaTestrig;
  public String deltaEnvironment;
  public boolean isDifferential = false;
  public WorkType workType = WorkType.UNKNOWN;

  public boolean isOverlappingInput(WorkDetails o) {

    if (baseTestrig.equals(o.baseTestrig) && baseEnvironment.equals(o.baseEnvironment)) {
      return true;
    }
    if (baseTestrig.equals(o.deltaTestrig) && baseEnvironment.equals(o.deltaEnvironment)) {
      return true;
    }
    if (isDifferential) {
      if (deltaTestrig.equals(o.baseTestrig) && deltaEnvironment.equals(o.baseEnvironment)) {
        return true;
      }
      if (deltaTestrig.equals(o.deltaTestrig) && deltaEnvironment.equals(o.deltaEnvironment)) {
        return true;
      }
    }
    return false;
  }
}
