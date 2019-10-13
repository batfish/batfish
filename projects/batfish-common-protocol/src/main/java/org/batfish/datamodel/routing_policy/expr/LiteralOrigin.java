package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.Environment;

/** Represents a specific constant {@link OriginType}. */
public final class LiteralOrigin extends OriginExpr {
  private static final String PROP_ORIGIN_TYPE = "originType";

  @Nullable private Long _asNum;

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
    } else if (!(obj instanceof LiteralOrigin)) {
      return false;
    }
    LiteralOrigin other = (LiteralOrigin) obj;
    return _originType == other._originType;
  }

  @Override
  public OriginType evaluate(Environment environment) {
    return _originType;
  }

  @Nullable
  public Long getAsNum() {
    return _asNum;
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  public OriginType getOriginType() {
    return _originType;
  }

  @Override
  public int hashCode() {
    return _originType.ordinal();
  }

  public void setAsNum(@Nullable Long asNum) {
    _asNum = asNum;
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  public void setOriginType(OriginType originType) {
    _originType = originType;
  }
}
