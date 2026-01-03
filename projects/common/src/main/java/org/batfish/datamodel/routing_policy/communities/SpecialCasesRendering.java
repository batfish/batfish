package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;

/**
 * A {@link CommunityRendering} with provided explicit mappings, that defers to another rendering
 * for keys not provided.
 */
public final class SpecialCasesRendering implements CommunityRendering {

  public static @Nonnull SpecialCasesRendering of(
      CommunityRendering fallbackRendering, Map<Community, String> specialCases) {
    return new SpecialCasesRendering(fallbackRendering, specialCases);
  }

  @Override
  public <T, U> T accept(CommunityRenderingVisitor<T, U> visitor, U arg) {
    return visitor.visitSpecialCasesRendering(this, arg);
  }

  /** The rendering to use for non-enumerated cases */
  @JsonProperty(PROP_FALLBACK_RENDERING)
  public @Nonnull CommunityRendering getFallbackRendering() {
    return _fallbackRendering;
  }

  /** Map from special-cased community to string representation */
  @JsonProperty(PROP_SPECIAL_CASES)
  @JsonDeserialize
  public @Nonnull Map<Community, String> getSpecialCases() {
    return _specialCases;
  }

  @JsonCreator
  private static @Nonnull SpecialCasesRendering create(
      @JsonProperty(PROP_FALLBACK_RENDERING) @Nullable CommunityRendering fallbackRendering,
      @JsonProperty(PROP_SPECIAL_CASES) @Nullable Map<Community, String> specialCases) {
    checkArgument(fallbackRendering != null, "Missing %s", PROP_FALLBACK_RENDERING);
    checkArgument(specialCases != null, "Missing %s", PROP_SPECIAL_CASES);
    return of(fallbackRendering, specialCases);
  }

  private SpecialCasesRendering(
      CommunityRendering fallbackRendering, Map<Community, String> specialCases) {
    _fallbackRendering = fallbackRendering;
    _specialCases = specialCases;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SpecialCasesRendering)) {
      return false;
    }
    SpecialCasesRendering rhs = (SpecialCasesRendering) obj;
    return _fallbackRendering.equals(rhs._fallbackRendering)
        && _specialCases.equals(rhs._specialCases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_fallbackRendering, _specialCases);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add(PROP_FALLBACK_RENDERING, _fallbackRendering)
        .add(PROP_SPECIAL_CASES, _specialCases)
        .toString();
  }

  private static final String PROP_FALLBACK_RENDERING = "fallbackRendering";
  private static final String PROP_SPECIAL_CASES = "specialCases";
  private final @Nonnull CommunityRendering _fallbackRendering;
  private final @Nonnull Map<Community, String> _specialCases;
}
