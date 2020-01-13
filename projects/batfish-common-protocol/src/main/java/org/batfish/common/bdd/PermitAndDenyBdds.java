package org.batfish.common.bdd;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.IpAccessList;

/**
 * Represents flows explicitly permitted and denied by some ACL-like object (e.g. {@link AclLine} or
 * {@link IpAccessList})
 */
@ParametersAreNonnullByDefault
public final class PermitAndDenyBdds {
  @Nonnull private final BDD _permitBdd;
  @Nonnull private final BDD _denyBdd;
  private BDD _matchBdd;

  public PermitAndDenyBdds(BDD permit, BDD deny) {
    _permitBdd = permit;
    _denyBdd = deny;
  }

  /**
   * Returns true if the lines represented by the given {@link PermitAndDenyBdds} can take different
   * actions on the same packet
   */
  public static boolean takeDifferentActions(PermitAndDenyBdds bdds1, PermitAndDenyBdds bdds2) {
    return bdds1.getPermitBdd().andSat(bdds2.getDenyBdd())
        || bdds1.getDenyBdd().andSat(bdds2.getPermitBdd());
  }

  /** BDD of all flows explicitly matched and permitted */
  @Nonnull
  public BDD getPermitBdd() {
    return _permitBdd;
  }

  /** BDD of all flows explicitly matched and denied */
  @Nonnull
  public BDD getDenyBdd() {
    return _denyBdd;
  }

  /** BDD of all explicitly matched flows, whether permitted or denied */
  @Nonnull
  public BDD getMatchBdd() {
    if (_matchBdd == null) {
      _matchBdd = _permitBdd.or(_denyBdd);
    }
    return _matchBdd;
  }

  /**
   * Returns a new {@link PermitAndDenyBdds} with BDDs created by {@link BDD#and anding} {@code bdd}
   * with this object's BDDs.
   */
  @Nonnull
  public PermitAndDenyBdds and(BDD bdd) {
    return new PermitAndDenyBdds(_permitBdd.and(bdd), _denyBdd.and(bdd));
  }

  /**
   * Returns a new {@link PermitAndDenyBdds} with BDDs created by subtracting {@code subtrahend}
   * from this object's BDDs.
   */
  @Nonnull
  public PermitAndDenyBdds diff(BDD subtrahend) {
    return new PermitAndDenyBdds(_permitBdd.diff(subtrahend), _denyBdd.diff(subtrahend));
  }

  public boolean isZero() {
    return _permitBdd.isZero() && _denyBdd.isZero();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof PermitAndDenyBdds)) {
      return false;
    }
    PermitAndDenyBdds o = (PermitAndDenyBdds) obj;
    return _permitBdd.equals(o._permitBdd) && _denyBdd.equals(o._denyBdd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_permitBdd, _denyBdd);
  }
}
