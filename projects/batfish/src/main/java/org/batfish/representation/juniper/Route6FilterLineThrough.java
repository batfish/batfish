package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix6;

public final class Route6FilterLineThrough extends Route6FilterLine {

  private final Prefix6 _throughPrefix6;

  public Route6FilterLineThrough(Prefix6 prefix6, Prefix6 throughPrefix6) {
    super(prefix6);
    _throughPrefix6 = throughPrefix6;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route6FilterLineThrough)) {
      return false;
    }

    Route6FilterLineThrough rhs = (Route6FilterLineThrough) o;
    return _prefix6.equals(rhs._prefix6) && _throughPrefix6.equals(rhs._throughPrefix6);
  }

  public Prefix6 getThroughPrefix6() {
    return _throughPrefix6;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _throughPrefix6.hashCode();
    result = prime * result + _prefix6.hashCode();
    return result;
  }
}
