package org.batfish.representation.cisco_asa;

import java.io.Serializable;

public class SecurityZonePair implements Serializable {

  private final String _dstZone;

  private String _inspectPolicyMap;

  private final String _name;

  private final String _srcZone;

  public SecurityZonePair(String name, String srcZone, String dstZone) {
    _name = name;
    _srcZone = srcZone;
    _dstZone = dstZone;
  }

  public String getDstZone() {
    return _dstZone;
  }

  public String getInspectPolicyMap() {
    return _inspectPolicyMap;
  }

  public String getName() {
    return _name;
  }

  public String getSrcZone() {
    return _srcZone;
  }

  public void setInspectPolicyMap(String inspectPolicyMap) {
    _inspectPolicyMap = inspectPolicyMap;
  }
}
