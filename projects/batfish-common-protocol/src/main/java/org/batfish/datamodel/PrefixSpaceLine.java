package org.batfish.datamodel;

public final class PrefixSpaceLine {

  private final LineAction _action;

  private final PrefixSpace _prefixSpace;

  public PrefixSpaceLine(PrefixSpace prefixSpace, LineAction action) {
    _prefixSpace = prefixSpace;
    _action = action;
  }

  public LineAction getAction() {
    return _action;
  }

  public PrefixSpace getPrefixSpace() {
    return _prefixSpace;
  }
}
