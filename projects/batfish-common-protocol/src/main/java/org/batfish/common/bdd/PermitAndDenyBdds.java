package org.batfish.common.bdd;

import javax.annotation.Nonnull;
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
   * Returns a new {@link PermitAndDenyBdds} with BDDs created by subtracting {@code subtrahend}
   * from this object's BDDs.
   */
  public PermitAndDenyBdds diff(BDD subtrahend) {
    return new PermitAndDenyBdds(_permitBdd.diff(subtrahend), _denyBdd.diff(subtrahend));
  }

  public boolean isZero() {
    return _permitBdd.isZero() && _denyBdd.isZero();
  }
}
