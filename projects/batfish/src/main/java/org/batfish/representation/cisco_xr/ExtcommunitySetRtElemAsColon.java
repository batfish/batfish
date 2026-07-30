package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A structure representing a space of route-target extended communities given by two range
 * expressions in the format 'R1:R2' for the global administrator and the local administrator
 * respectively.
 *
 * <p>Per RFC 4360, exactly one administrator may be 4 bytes wide: a type-2 route target has a
 * 4-byte global administrator and a 2-byte local administrator, while a type-0 route target has a
 * 2-byte global administrator and a 4-byte local administrator. Both are represented here as 32-bit
 * range expressions; the type is derived from the values when converting, as in {@link
 * org.batfish.datamodel.bgp.community.ExtendedCommunity#target}.
 */
@ParametersAreNonnullByDefault
public final class ExtcommunitySetRtElemAsColon implements ExtcommunitySetRtElem {

  public ExtcommunitySetRtElemAsColon(Uint32RangeExpr gaRangeExpr, Uint32RangeExpr laRangeExpr) {
    _gaRangeExpr = gaRangeExpr;
    _laRangeExpr = laRangeExpr;
  }

  @Override
  public <T, U> T accept(ExtcommunitySetRtElemVisitor<T, U> visitor, U arg) {
    return visitor.visitExtcommunitySetRtElemAsColon(this, arg);
  }

  public @Nonnull Uint32RangeExpr getGaRangeExpr() {
    return _gaRangeExpr;
  }

  public @Nonnull Uint32RangeExpr getLaRangeExpr() {
    return _laRangeExpr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExtcommunitySetRtElemAsColon)) {
      return false;
    }
    ExtcommunitySetRtElemAsColon rhs = (ExtcommunitySetRtElemAsColon) obj;
    return _gaRangeExpr.equals(rhs._gaRangeExpr) && _laRangeExpr.equals(rhs._laRangeExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_gaRangeExpr, _laRangeExpr);
  }

  private final @Nonnull Uint32RangeExpr _gaRangeExpr;
  private final @Nonnull Uint32RangeExpr _laRangeExpr;
}
