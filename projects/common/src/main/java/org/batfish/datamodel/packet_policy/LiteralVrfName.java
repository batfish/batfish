package org.batfish.datamodel.packet_policy;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a literal VRF name */
public final class LiteralVrfName implements VrfExpr {
  private static final String PROP_VRF_NAME = "vrfName";

  private final @Nonnull String _vrfName;

  public LiteralVrfName(String vrfName) {
    _vrfName = vrfName;
  }

  @JsonProperty(PROP_VRF_NAME)
  public @Nonnull String getVrfName() {
    return _vrfName;
  }

  @Override
  public <T> T accept(VrfExprVisitor<T> visitor) {
    return visitor.visitLiteralVrfName(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LiteralVrfName)) {
      return false;
    }
    LiteralVrfName that = (LiteralVrfName) o;
    return _vrfName.equals(that._vrfName);
  }

  @Override
  public int hashCode() {
    return _vrfName.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(LiteralVrfName.class).add("_vrfName", _vrfName).toString();
  }

  @JsonCreator
  private static LiteralVrfName jsonCreator(@JsonProperty(PROP_VRF_NAME) @Nullable String vrfName) {
    checkArgument(vrfName != null, "Missing %s", PROP_VRF_NAME);
    return new LiteralVrfName(vrfName);
  }
}
