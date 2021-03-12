package org.batfish.representation.cisco_asa;

public class NamedBgpPeerGroup extends BgpPeerGroup {

  private String _name;

  public NamedBgpPeerGroup(String name) {
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }
}
