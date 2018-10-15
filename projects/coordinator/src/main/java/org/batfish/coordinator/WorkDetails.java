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
  public final String deltaTestrig;
  public final boolean isDifferential;
  public final WorkType workType;

  public WorkDetails(String baseTestrig, WorkType workType) {
    this(baseTestrig, null, false, workType);
  }

  public WorkDetails(
      String baseTestrig, String deltaTestrig, boolean isDifferential, WorkType workType) {
    this.baseTestrig = baseTestrig;
    this.deltaTestrig = deltaTestrig;
    this.isDifferential = isDifferential;
    this.workType = workType;
  }

  public boolean isOverlappingInput(WorkDetails o) {
    return baseTestrig.equals(o.baseTestrig)
        || baseTestrig.equals(o.deltaTestrig)
        || isDifferential
            && (deltaTestrig.equals(o.baseTestrig) || deltaTestrig.equals(o.deltaTestrig));
  }
}
