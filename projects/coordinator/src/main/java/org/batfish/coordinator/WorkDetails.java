package org.batfish.coordinator;

public class WorkDetails {

  public enum WorkType {
    PARSING,
    DATAPLANING,
    INDEPENDENT_ANSWERING, // answering includes analyzing
    PARSING_DEPENDENT_ANSWERING,
    DATAPLANE_DEPENDENT_ANSWERING,
    UNKNOWN
  }

  public final String baseTestrig;
  public final String baseEnv;
  public final String deltaTestrig;
  public final String deltaEnv;
  public final boolean isDifferential;
  public final WorkType workType;

  public WorkDetails() {
    this(null, null, null, null, false, WorkType.UNKNOWN);
  }

  public WorkDetails(String baseTestrig, String baseEnv, WorkType workType) {
    this(baseTestrig, baseEnv, null, null, false, workType);
  }

  public WorkDetails(
      String baseTestrig,
      String baseEnv,
      String deltaTestrig,
      String deltaEnv,
      boolean isDifferential,
      WorkType workType) {
    this.baseTestrig = baseTestrig;
    this.baseEnv = baseEnv;
    this.deltaTestrig = deltaTestrig;
    this.deltaEnv = deltaEnv;
    this.isDifferential = isDifferential;
    this.workType = workType;
  }

  public boolean isOverlappingInput(WorkDetails o) {

    if (baseTestrig.equals(o.baseTestrig) && baseEnv.equals(o.baseEnv)) {
      return true;
    }
    if (baseTestrig.equals(o.deltaTestrig) && baseEnv.equals(o.deltaEnv)) {
      return true;
    }
    if (isDifferential) {
      if (deltaTestrig.equals(o.baseTestrig) && deltaEnv.equals(o.baseEnv)) {
        return true;
      }
      if (deltaTestrig.equals(o.deltaTestrig) && deltaEnv.equals(o.deltaEnv)) {
        return true;
      }
    }
    return false;
  }
}
