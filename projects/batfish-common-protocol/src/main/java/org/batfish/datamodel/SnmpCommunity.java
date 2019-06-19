package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class SnmpCommunity extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private String _accessList;

  private String _accessList6;

  private boolean _ro;

  private boolean _rw;

  public SnmpCommunity(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }

  public String getAccessList() {
    return _accessList;
  }

  public String getAccessList6() {
    return _accessList6;
  }

  public boolean getRo() {
    return _ro;
  }

  public boolean getRw() {
    return _rw;
  }

  public void setAccessList(String accessList) {
    _accessList = accessList;
  }

  public void setAccessList6(String accessList6) {
    _accessList6 = accessList6;
  }

  public void setRo(boolean ro) {
    _ro = ro;
  }

  public void setRw(boolean rw) {
    _rw = rw;
  }
}
