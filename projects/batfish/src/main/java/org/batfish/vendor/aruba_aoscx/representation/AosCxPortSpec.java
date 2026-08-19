package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Layer-4 port match in an Aruba AOS-CX IPv4 ACE. */
public final class AosCxPortSpec implements Serializable {

  public enum Operator {
    EQ,
    GT,
    LT,
    RANGE
  }

  public AosCxPortSpec(Operator operator, int first, @Nullable Integer second) {
    _operator = operator;
    _first = first;
    _second = second;
  }

  public Operator getOperator() {
    return _operator;
  }

  public int getFirst() {
    return _first;
  }

  public @Nullable Integer getSecond() {
    return _second;
  }

  private final Operator _operator;
  private final int _first;
  private final @Nullable Integer _second;
}
