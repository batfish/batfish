package org.batfish.representation.cisco;

public class LiteralCommunitySetElemHalf implements CommunitySetElemHalfExpr {

  private static final long serialVersionUID = 1L;

  private final int _value;

  public LiteralCommunitySetElemHalf(int value) {
    _value = value;
  }

  public int getValue() {
    return _value;
  }
}
