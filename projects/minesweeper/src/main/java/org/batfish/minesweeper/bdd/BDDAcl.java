package org.batfish.minesweeper.bdd;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

/** Representation of an Access Control List (ACL) as a Binary Decision Diagram (BDD). */
@ParametersAreNonnullByDefault
public final class BDDAcl {
  @Nonnull private BDD _bdd;

  @Nonnull private final BDDPacket _pkt;

  private BDDAcl(BDD bdd, BDDPacket pkt) {
    _bdd = bdd;
    _pkt = pkt;
  }

  private BDDAcl(BDDAcl other) {
    _bdd = other._bdd;
    _pkt = other._pkt;
  }

  /**
   * Create a new BDD given an ACL
   *
   * @param pkt a {@link BDDPacket} -- a collection of attributes to use in a BDD
   * @param acl the {@link IpAccessList} to represent
   */
  public static BDDAcl create(BDDPacket pkt, IpAccessList acl) {
    return create(pkt, acl, ImmutableMap.of(), ImmutableMap.of(), BDDSourceManager.empty(pkt));
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
    return create(pkt, acl, aclEnv, ipSpaceEnv, BDDSourceManager.empty(pkt));
  }

  public static BDDAcl create(
      BDDPacket pkt,
      IpAccessList acl,
      Map<String, IpAccessList> aclEnv,
      Map<String, IpSpace> ipSpaceEnv,
      BDDSourceManager bddSrcManager) {
    BDD bdd = new IpAccessListToBddImpl(pkt, bddSrcManager, aclEnv, ipSpaceEnv).toBdd(acl);
    return new BDDAcl(bdd, pkt);
  }

  @Nonnull
  public BDD getBdd() {
    return _bdd;
  }

  @Nonnull
  public BDDFactory getFactory() {
    return _pkt.getFactory();
  }

  @Nonnull
  public BDDPacket getPkt() {
    return _pkt;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_bdd);
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
    other._bdd = _pkt.restrict(_bdd, pfx);
    return other;
  }

  /** Create a new version of the BDD restricted to a list of prefixes */
  public BDDAcl restrict(List<Prefix> prefixes) {
    BDDAcl other = new BDDAcl(this);
    other._bdd = _pkt.restrict(_bdd, prefixes);
    return other;
  }
}
