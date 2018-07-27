package org.batfish.z3;

import org.batfish.common.Pair;

public class AclIdentifier extends Pair<String, String> {

  private static final long serialVersionUID = 1L;

  public AclIdentifier(String hostname, String aclName) {
    super(hostname, aclName);
  }

  public String getAclName() {
    return _second;
  }

  public String getHostname() {
    return _first;
  }
}
