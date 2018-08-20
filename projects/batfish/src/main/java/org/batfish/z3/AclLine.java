package org.batfish.z3;

import org.batfish.common.Pair;

public class AclLine extends Pair<String, Pair<String, Integer>> {

  /** */
  private static final long serialVersionUID = 1L;

  public AclLine(String hostname, String aclName, int line) {
    super(hostname, new Pair<>(aclName, line));
  }

  public String getAclName() {
    return _second.getFirst();
  }

  public String getHostname() {
    return _first;
  }

  public int getLine() {
    return _second.getSecond();
  }

  public AclIdentifier toAclIdentifier() {
    return new AclIdentifier(_first, _second.getFirst());
  }
}
