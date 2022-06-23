package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix6;

public class Route6FilterLineOrLonger extends Route6FilterLine {

  public Route6FilterLineOrLonger(Prefix6 prefix6) {
    super(prefix6);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Route6FilterLineOrLonger)) {
      return false;
    }

    Route6FilterLineOrLonger rhs = (Route6FilterLineOrLonger) o;
    return _prefix6.equals(rhs._prefix6);
  }

  @Override
  public int hashCode() {
    return _prefix6.hashCode();
  }
}
