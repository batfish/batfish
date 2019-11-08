package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A structure representing a space of route-target extended communities given by three 16-bit range
 * expressions in the format 'R1.R2:R3' for the high and low 16 bits of the global administrator;
 * and the 16-bits of the local administrator.
 */
@ParametersAreNonnullByDefault
public final class ExtcommunitySetRtElemAsDotColon implements ExtcommunitySetRtElem {

  public ExtcommunitySetRtElemAsDotColon(
      Uint16RangeExpr gaHighRangeExpr,
      Uint16RangeExpr gaLowRangeExpr,
      Uint16RangeExpr laRangeExpr) {
    _gaHighRangeExpr = gaHighRangeExpr;
    _gaLowRangeExpr = gaLowRangeExpr;
    _laRangeExpr = laRangeExpr;
  }

  @Override
  public <T, U> T accept(ExtcommunitySetRtElemVisitor<T, U> visitor, U arg) {
    return visitor.visitExtcommunitySetRtElemAsDotColon(this, arg);
  }

  public @Nonnull Uint16RangeExpr getGaHighRangeExpr() {
    return _gaHighRangeExpr;
  }

  public @Nonnull Uint16RangeExpr getGaLowRangeExpr() {
    return _gaLowRangeExpr;
  }

  public @Nonnull Uint16RangeExpr getLaRangeExpr() {
    return _laRangeExpr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExtcommunitySetRtElemAsDotColon)) {
      return false;
    }
    ExtcommunitySetRtElemAsDotColon rhs = (ExtcommunitySetRtElemAsDotColon) obj;
    return _gaHighRangeExpr.equals(rhs._gaHighRangeExpr)
        && _gaLowRangeExpr.equals(rhs._gaLowRangeExpr)
        && _laRangeExpr.equals(rhs._laRangeExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_gaHighRangeExpr, _gaLowRangeExpr, _laRangeExpr);
  }

  private final @Nonnull Uint16RangeExpr _gaHighRangeExpr;
  private final @Nonnull Uint16RangeExpr _gaLowRangeExpr;
  private final @Nonnull Uint16RangeExpr _laRangeExpr;
}
