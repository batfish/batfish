package org.batfish.datamodel.packet_policy;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Switch to a regular destination-based forwarding pipeline. That is, perform a FIB lookup in a
 * given VRF, use the results to forward packet.
 */
public final class FibLookup implements Action {

  private static final String PROP_VRF_EXPR = "vrfExpr";

  private final @Nonnull VrfExpr _vrfExpr;

  public FibLookup(VrfExpr vrfExpr) {
    _vrfExpr = vrfExpr;
  }

  @JsonCreator
  private static FibLookup jsonCreator(@JsonProperty(PROP_VRF_EXPR) @Nullable VrfExpr vrfName) {
    checkArgument(vrfName != null, "Missing %s", PROP_VRF_EXPR);
    return new FibLookup(vrfName);
  }

  @JsonProperty(PROP_VRF_EXPR)
  public @Nonnull VrfExpr getVrfExpr() {
    return _vrfExpr;
  }

  @Override
  public <T> T accept(ActionVisitor<T> visitor) {
    return visitor.visitFibLookup(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FibLookup)) {
      return false;
    }
    FibLookup fibLookup = (FibLookup) o;
    return _vrfExpr.equals(fibLookup._vrfExpr);
  }

  @Override
  public int hashCode() {
    return _vrfExpr.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(FibLookup.class).add(PROP_VRF_EXPR, _vrfExpr).toString();
  }
}
