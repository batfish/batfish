package org.batfish.datamodel.answers;

public enum ParseStatus {
  EMPTY,
  FAILED,
  IGNORED,
  ORPHANED,
  PARTIALLY_UNRECOGNIZED,
  PASSED,
  UNKNOWN,
  UNSUPPORTED
}
