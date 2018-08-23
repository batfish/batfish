package org.batfish.symbolic.bdd;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.util.NonRecursiveSupplier;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;

/** Representation of an Access Control List (ACL) as a Binary Decision Diagram (BDD). */
@ParametersAreNonnullByDefault
public final class BDDAcl {

  @Nonnull private final Map<String, Supplier<BDD>> _aclEnv;

  @Nonnull private BDD _bdd;

  @Nonnull private final BDDSourceManager _bddSrcManager;

  @Nonnull private final BDDFactory _factory;

  @Nonnull private final Map<String, IpSpace> _ipSpaceEnv;

  @Nonnull private final BDDPacket _pkt;

  private BDDAcl(
      BDDPacket pkt,
      @Nullable IpAccessList acl,
      Map<String, Supplier<BDD>> aclEnv,
      Map<String, IpSpace> ipSpaceEnv,
      BDDSourceManager bddSrcManager) {
    _aclEnv = ImmutableMap.copyOf(aclEnv);
    _bddSrcManager = bddSrcManager;
    _pkt = pkt;
    _factory = _pkt.getFactory();
    _ipSpaceEnv = ImmutableMap.copyOf(ipSpaceEnv);
    _bdd = computeACL(acl);
  }

  private BDDAcl(BDDAcl other) {
    _bdd = other._bdd;
    _aclEnv = other._aclEnv;
    _bddSrcManager = other._bddSrcManager;
    _factory = other._factory;
    _ipSpaceEnv = ImmutableMap.copyOf(other._ipSpaceEnv);
    _pkt = other._pkt;
  }

  /**
   * Create a new BDD given an ACL
   *
   * @param pkt a {@link BDDPacket} -- a collection of attributes to use in a BDD
   * @param acl the {@link IpAccessList} to represent
   */
  public static BDDAcl create(BDDPacket pkt, IpAccessList acl) {
    return create(
        pkt,
        acl,
        ImmutableMap.of(),
        ImmutableMap.of(),
        BDDSourceManager.forInterfaces(pkt, ImmutableSet.of()));
  }

  /**
   * Create a new BDD given an ACL
   *
   * @param pkt a {@link BDDPacket} -- a collection of attributes to use in a BDD
   * @param acl the {@link IpAccessList} to represent
   * @param aclEnv a map of ACL names to {@link IpAccessList}. Used when original {@code acl} refers
   *     to other ACLs.
   * @param ipSpaceEnv a map of names to named IpSpaces.
   */
  public static BDDAcl create(
      BDDPacket pkt,
      IpAccessList acl,
      Map<String, IpAccessList> aclEnv,
      Map<String, IpSpace> ipSpaceEnv) {
    return create(
        pkt, acl, aclEnv, ipSpaceEnv, BDDSourceManager.forInterfaces(pkt, ImmutableSet.of()));
  }

  public static BDDAcl create(
      BDDPacket pkt,
      IpAccessList acl,
      Map<String, IpAccessList> aclEnv,
      Map<String, IpSpace> ipSpaceEnv,
      BDDSourceManager bddSrcManager) {
    // use laziness to tie the recursive knot.
    Map<String, Supplier<BDD>> bddAclEnv = new HashMap<>();
    aclEnv.forEach(
        (name, namedAcl) ->
            bddAclEnv.put(
                name,
                Suppliers.memoize(
                    new NonRecursiveSupplier<>(
                        () ->
                            createWithBDDAclEnv(pkt, namedAcl, bddAclEnv, ipSpaceEnv, bddSrcManager)
                                ._bdd))));

    return new BDDAcl(pkt, acl, bddAclEnv, ipSpaceEnv, bddSrcManager);
  }

  private static BDDAcl createWithBDDAclEnv(
      BDDPacket pkt,
      IpAccessList acl,
      Map<String, Supplier<BDD>> aclEnv,
      Map<String, IpSpace> ipSpaceEnv,
      BDDSourceManager bddSrcManager) {
    return new BDDAcl(pkt, acl, aclEnv, ipSpaceEnv, bddSrcManager);
  }

  /**
   * Convert an Access Control List (ACL) to a symbolic boolean expression. The default action in an
   * ACL is to deny all traffic.
   */
  @Nonnull
  private BDD computeACL(@Nullable IpAccessList acl) {
    // Check if there is an ACL first
    if (acl == null) {
      return _factory.one();
    }

    BDD result = _factory.zero();
    AclLineMatchExprToBDD aclLineMatchExprToBDD =
        new AclLineMatchExprToBDD(_factory, _pkt, _aclEnv, _ipSpaceEnv, _bddSrcManager);

    for (IpAccessListLine line : Lists.reverse(acl.getLines())) {
      BDD lineBDD = aclLineMatchExprToBDD.visit(line.getMatchCondition());
      BDD actionBDD = line.getAction() == LineAction.PERMIT ? _factory.one() : _factory.zero();
      result = lineBDD.ite(actionBDD, result);
    }
    return result;
  }

  @Nonnull
  public BDD getBdd() {
    return _bdd;
  }

  @Nonnull
  public BDDFactory getFactory() {
    return _factory;
  }

  @Nonnull
  public BDDPacket getPkt() {
    return _pkt;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_bdd);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BDDAcl)) {
      return false;
    }
    BDDAcl other = (BDDAcl) o;
    return Objects.equals(_bdd, other._bdd);
  }

  /** Create a new version of the BDD restricted to a prefix */
  public BDDAcl restrict(Prefix pfx) {
    BDDAcl other = new BDDAcl(this);
    other._bdd = this._pkt.restrict(this._bdd, pfx);
    return other;
  }

  /** Create a new version of the BDD restricted to a list of prefixes */
  public BDDAcl restrict(List<Prefix> prefixes) {
    BDDAcl other = new BDDAcl(this);
    other._bdd = this._pkt.restrict(this._bdd, prefixes);
    return other;
  }
}
