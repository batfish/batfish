package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.Environment;

public class LiteralOrigin extends OriginExpr {
  private static final String PROP_ORIGIN_TYPE = "originType";

  private @Nullable Long _asNum;

  private OriginType _originType;

  @JsonCreator
  private LiteralOrigin() {}

  public LiteralOrigin(OriginType originType, @Nullable Long asNum) {
    _asNum = asNum;
    _originType = originType;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LiteralOrigin other = (LiteralOrigin) obj;
    if (_asNum == null) {
      if (other._asNum != null) {
        return false;
      }
    } else if (!_asNum.equals(other._asNum)) {
      return false;
    }
    if (_originType != other._originType) {
      return false;
    }
    return true;
  }

  @Override
  public OriginType evaluate(Environment environment) {
    return _originType;
  }

  public @Nullable Long getAsNum() {
    return _asNum;
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  public OriginType getOriginType() {
    return _originType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_asNum == null) ? 0 : _asNum.hashCode());
    result = prime * result + ((_originType == null) ? 0 : _originType.ordinal());
    return result;
  }

  public void setAsNum(@Nullable Long asNum) {
    _asNum = asNum;
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  public void setOriginType(OriginType originType) {
    _originType = originType;
  }
}
