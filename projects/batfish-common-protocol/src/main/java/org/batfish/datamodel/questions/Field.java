package org.batfish.datamodel.questions;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Field {

  private boolean _optional;

  public boolean getOptional() {
    return _optional;
  }

  public void setOptional(boolean optional) {
    _optional = optional;
  }
}
