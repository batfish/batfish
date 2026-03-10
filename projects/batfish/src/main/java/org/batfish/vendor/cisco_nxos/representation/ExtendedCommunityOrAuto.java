package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

/** Either {@code auto} or an explicit {@link ExtendedCommunity}. */
public final class ExtendedCommunityOrAuto implements Serializable {

  private static final ExtendedCommunityOrAuto AUTO = new ExtendedCommunityOrAuto(null);

  public static ExtendedCommunityOrAuto auto() {
    return AUTO;
  }

  public static ExtendedCommunityOrAuto of(@Nonnull ExtendedCommunity extendedCommunity) {
    return new ExtendedCommunityOrAuto(extendedCommunity);
  }

  public boolean isAuto() {
    return _extendedCommunity == null;
  }

  public @Nullable ExtendedCommunity getExtendedCommunity() {
    return _extendedCommunity;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private ExtendedCommunityOrAuto(@Nullable ExtendedCommunity ec) {
    _extendedCommunity = ec;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ExtendedCommunityOrAuto)) {
      return false;
    }
    ExtendedCommunityOrAuto that = (ExtendedCommunityOrAuto) o;
    return Objects.equals(_extendedCommunity, that._extendedCommunity);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_extendedCommunity);
  }

  private final @Nullable ExtendedCommunity _extendedCommunity;
}
