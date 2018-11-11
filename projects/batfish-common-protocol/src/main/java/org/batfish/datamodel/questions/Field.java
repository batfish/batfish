package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;

/** Settings for field of complex {@link org.batfish.datamodel.questions.Variable}. */
@ParametersAreNonnullByDefault
public final class Field {

  private boolean _optional;

  @JsonProperty(BfConsts.PROP_OPTIONAL)
  public boolean getOptional() {
    return _optional;
  }

  @JsonProperty(BfConsts.PROP_OPTIONAL)
  public void setOptional(boolean optional) {
    _optional = optional;
  }
}
