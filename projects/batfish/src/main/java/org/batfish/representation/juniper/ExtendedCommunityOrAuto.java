package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/**
 * Represents either an explicit {@link ExtendedCommunity} or the keyword {@code auto} for
 * vrf-target configuration.
 */
public final class ExtendedCommunityOrAuto implements Serializable {

  private final @Nullable ExtendedCommunity _extendedCommunity;

  private ExtendedCommunityOrAuto(@Nullable ExtendedCommunity extendedCommunity) {
    _extendedCommunity = extendedCommunity;
  }

  /** Creates an instance representing the {@code auto} keyword. */
  public static ExtendedCommunityOrAuto auto() {
    return new ExtendedCommunityOrAuto(null);
  }

  /** Creates an instance representing an explicit extended community. */
  public static ExtendedCommunityOrAuto of(ExtendedCommunity extendedCommunity) {
    return new ExtendedCommunityOrAuto(extendedCommunity);
  }

  public boolean isAuto() {
    return _extendedCommunity == null;
  }

  public @Nullable ExtendedCommunity getExtendedCommunity() {
    return _extendedCommunity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExtendedCommunityOrAuto that)) {
      return false;
    }
    return Objects.equals(_extendedCommunity, that._extendedCommunity);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_extendedCommunity);
  }
}
