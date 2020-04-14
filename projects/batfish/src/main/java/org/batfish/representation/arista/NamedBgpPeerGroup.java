package org.batfish.representation.arista;

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
