package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Matches a {@link org.batfish.datamodel.bgp.community.Community} iff it is an opaque extended
 * community of the given type specifications.
 */
public final class OpaqueExtendedCommunities extends CommunityMatchExpr {
  private final boolean _isTransitive;
  private final int _subType;

  private OpaqueExtendedCommunities(boolean isTransitive, int subType) {
    _isTransitive = isTransitive;
    _subType = subType;
  }

  public static OpaqueExtendedCommunities of(boolean isTransitive, int subType) {
    return new OpaqueExtendedCommunities(isTransitive, subType);
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitOpaqueExtendedCommunities(this, arg);
  }

  @JsonProperty(PROP_TRANSITIVE)
  public boolean getIsTransitive() {
    return _isTransitive;
  }

  @JsonProperty(PROP_SUBTYPE)
  public int getSubtype() {
    return _subType;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OpaqueExtendedCommunities)) {
      return false;
    }
    OpaqueExtendedCommunities that = (OpaqueExtendedCommunities) obj;
    return _isTransitive == that._isTransitive && _subType == that._subType;
  }

  private static final String PROP_TRANSITIVE = "transitive";
  private static final String PROP_SUBTYPE = "subType";

  @Override
  public int hashCode() {
    return Objects.hash(_isTransitive, _subType);
  }

  @JsonCreator
  private static @Nonnull OpaqueExtendedCommunities jsonCreator(
      @JsonProperty(PROP_TRANSITIVE) boolean isTransitive,
      @JsonProperty(PROP_SUBTYPE) int subType) {
    return OpaqueExtendedCommunities.of(isTransitive, subType);
  }
}
