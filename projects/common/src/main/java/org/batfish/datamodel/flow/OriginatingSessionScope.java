package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a session that can match flows originating in a given VRF */
@ParametersAreNonnullByDefault
public final class OriginatingSessionScope implements SessionScope {
  private static final String PROP_ORIGINATING_VRF = "originatingVrf";

  private final @Nonnull String _originatingVrf;

  public OriginatingSessionScope(String originatingVrf) {
    _originatingVrf = originatingVrf;
  }

  @JsonCreator
  private static OriginatingSessionScope jsonCreator(
      @JsonProperty(PROP_ORIGINATING_VRF) @Nullable String originatingVrf) {
    checkNotNull(originatingVrf, "Missing %s", PROP_ORIGINATING_VRF);
    return new OriginatingSessionScope(originatingVrf);
  }

  @Override
  public <T> T accept(SessionScopeVisitor<T> visitor) {
    return visitor.visitOriginatingSessionScope(this);
  }

  @JsonProperty(PROP_ORIGINATING_VRF)
  public @Nonnull String getOriginatingVrf() {
    return _originatingVrf;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof OriginatingSessionScope)) {
      return false;
    }
    return _originatingVrf.equals(((OriginatingSessionScope) obj)._originatingVrf);
  }

  @Override
  public int hashCode() {
    return _originatingVrf.hashCode();
  }
}
