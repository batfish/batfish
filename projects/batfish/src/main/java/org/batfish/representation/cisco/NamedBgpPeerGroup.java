package org.batfish.representation.cisco;

public class NamedBgpPeerGroup extends BgpPeerGroup {

  private static final long serialVersionUID = 1L;

  private final int _definitionLine;

  private String _name;

  public NamedBgpPeerGroup(String name, int definitionLine) {
    _name = name;
    _definitionLine = definitionLine;
  }

  public int getDefinitionLine() {
    return _definitionLine;
  }

  @Override
  public String getName() {
    return _name;
  }
}
