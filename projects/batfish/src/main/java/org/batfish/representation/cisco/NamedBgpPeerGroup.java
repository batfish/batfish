package org.batfish.representation.cisco;

public class NamedBgpPeerGroup extends BgpPeerGroup {

  private static final long serialVersionUID = 1L;

  private String _name;

  public NamedBgpPeerGroup(String name) {
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }
}
