package org.batfish.vendor;

import java.io.Serializable;

public final class ConversionContext implements Serializable {
  @Override
  public boolean equals(Object obj) {
    return obj instanceof ConversionContext;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
