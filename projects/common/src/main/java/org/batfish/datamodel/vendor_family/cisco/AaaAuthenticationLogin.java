package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.AaaAuthenticationLoginList;

public class AaaAuthenticationLogin implements Serializable {

  public static final String DEFAULT_LIST_NAME = "default";

  private SortedMap<String, AaaAuthenticationLoginList> _lists;

  private boolean _privilegeMode;

  public AaaAuthenticationLogin() {
    _lists = new TreeMap<>();
  }

  public SortedMap<String, AaaAuthenticationLoginList> getLists() {
    return _lists;
  }

  public boolean getPrivilegeMode() {
    return _privilegeMode;
  }

  public void setLists(SortedMap<String, AaaAuthenticationLoginList> lists) {
    _lists = lists;
  }

  public void setPrivilegeMode(boolean privilegeMode) {
    _privilegeMode = privilegeMode;
  }
}
