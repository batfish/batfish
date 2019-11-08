package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A structure representing a space of route-target extended communities given by a 32-bit range
 * expression and 16-bit range expression in the format 'R1:R2' for the 32 bits of the global
 * administrator and the 16 bits of the local administrator respectively.
 */
@ParametersAreNonnullByDefault
public final class ExtcommunitySetRtElemAsColon implements ExtcommunitySetRtElem {

  public ExtcommunitySetRtElemAsColon(Uint32RangeExpr gaRangeExpr, Uint16RangeExpr laRangeExpr) {
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

  public @Nonnull Uint16RangeExpr getLaRangeExpr() {
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
  private final @Nonnull Uint16RangeExpr _laRangeExpr;
}
