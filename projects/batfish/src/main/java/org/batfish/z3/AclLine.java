package org.batfish.z3;

import org.batfish.common.Pair;

public class AclLine extends Pair<String, Pair<String, Integer>> {

  /** */
  private static final long serialVersionUID = 1L;

  private Integer _earliestMoreGeneralReachableLine;

  public AclLine(String hostname, String aclName, int line) {
    super(hostname, new Pair<>(aclName, line));
  }

  public String getAclName() {
    return _second.getFirst();
  }

  public Integer getEarliestMoreGeneralReachableLine() {
    return _earliestMoreGeneralReachableLine;
  }

  public String getHostname() {
    return _first;
  }

  public int getLine() {
    return _second.getSecond();
  }

  public void setEarliestMoreGeneralReachableLine(Integer earliestMoreGeneralReachableLine) {
    _earliestMoreGeneralReachableLine = earliestMoreGeneralReachableLine;
  }
}
