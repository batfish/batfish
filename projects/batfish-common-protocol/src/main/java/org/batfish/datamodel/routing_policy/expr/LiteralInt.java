package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class LiteralInt extends IntExpr {

  private static final String PROP_VALUE = "value";

  /** */
  private static final long serialVersionUID = 1L;

  private int _value;

  @JsonCreator
  private static LiteralInt jsonCreator(@Nullable @JsonProperty(PROP_VALUE) Integer value) {
    checkArgument(value != null, "%s must be provided", PROP_VALUE);
    return new LiteralInt(value);
  }

  public LiteralInt(int value) {
    _value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof LiteralInt)) {
      return false;
    }
    LiteralInt other = (LiteralInt) obj;
    return _value == other._value;
  }

  @Override
  public int evaluate(Environment environment) {
    return _value;
  }

  @JsonProperty(PROP_VALUE)
  public int getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _value;
    return result;
  }

  public void setValue(int value) {
    _value = value;
  }
}
