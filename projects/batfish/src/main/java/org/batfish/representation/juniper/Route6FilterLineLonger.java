package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix6;

public class Route6FilterLineLonger extends Route6FilterLine {

  public Route6FilterLineLonger(Prefix6 prefix) {
    super(prefix);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route6FilterLineLonger)) {
      return false;
    }

    Route6FilterLineLonger rhs = (Route6FilterLineLonger) o;
    return _prefix6.equals(rhs._prefix6);
  }

  @Override
  public int hashCode() {
    return _prefix6.hashCode();
  }
}
