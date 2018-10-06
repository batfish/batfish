package org.batfish.representation.cisco;

public class NamedBgpPeerGroup extends BgpPeerGroup {

  private static final long serialVersionUID = 1L;

  private final int _definitionLine;

  private String _name;

  public NamedBgpPeerGroup(String name) {
    _name = name;
    _definitionLine = -1;
  }

  public int getDefinitionLine() {
    return _definitionLine;
  }

  @Override
  public String getName() {
    return _name;
  }
}
