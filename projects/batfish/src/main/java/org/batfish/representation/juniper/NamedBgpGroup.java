package org.batfish.representation.juniper;

public class NamedBgpGroup extends BgpGroup {

  public NamedBgpGroup(String name) {
    _groupName = name;
  }

  public boolean getInherited() {
    return _inherited;
  }

  public String getName() {
    return _groupName;
  }
}
