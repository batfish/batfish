package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A flow being delegated to a different VRF for further processing. */
public final class DelegatedToNextVrf implements ForwardingDetail {

  public static @Nonnull DelegatedToNextVrf of(String nextVrf) {
    return new DelegatedToNextVrf(nextVrf);
  }

  private DelegatedToNextVrf(String nextVrf) {
    _nextVrf = nextVrf;
  }

  @JsonCreator
  private static @Nonnull DelegatedToNextVrf create(
      @JsonProperty(PROP_NEXT_VRF) @Nullable String nextVrf) {
    checkArgument(nextVrf != null, "Missing %s", PROP_NEXT_VRF);
    return of(nextVrf);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof DelegatedToNextVrf)) {
      return false;
    }
    DelegatedToNextVrf that = (DelegatedToNextVrf) o;
    return _nextVrf.equals(that._nextVrf);
  }

  @Override
  public int hashCode() {
    return _nextVrf.hashCode();
  }

  @JsonProperty(PROP_NEXT_VRF)
  public @Nonnull String getNextVrf() {
    return _nextVrf;
  }

  private static final String PROP_NEXT_VRF = "nextVrf";

  private final @Nonnull String _nextVrf;
}
