package org.batfish.symbolic.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpSpaceToBDD implements GenericIpSpaceVisitor<BDD> {

  private final BDDFactory _factory;

  private final BDD[] _bitBDDs;

  public IpSpaceToBDD(BDDFactory factory, BDDInteger var) {
    _factory = factory;
    _bitBDDs = var.getBitvec();
  }

  @Override
  public BDD castToGenericIpSpaceVisitorReturnType(Object o) {
    return (BDD) o;
  }

  /*
   * Check if the first length bits match the BDDInteger
   * representing the advertisement prefix.
   *
   * Note: We assume the prefix is never modified, so it will
   * be a bitvector containing only the underlying variables:
   * [var(0), ..., var(n)]
   */
  private BDD firstBitsEqual(Ip ip, int length) {
    long b = ip.asLong();
    BDD acc = _factory.one();
    for (int i = 0; i < length; i++) {
      boolean bitValue = Ip.getBitAtPosition(b, i);
      if (bitValue) {
        acc = acc.and(_bitBDDs[i]);
      } else {
        acc = acc.and(_bitBDDs[i].not());
      }
    }
    return acc;
  }

  /*
   * Does the 32 bit integer match the prefix using lpm?
   */
  private BDD isRelevantFor(Prefix p) {
    return firstBitsEqual(p.getStartIp(), p.getPrefixLength());
  }

  public BDD toBDD(Ip ip) {
    return toBDD(new Prefix(ip, Prefix.MAX_PREFIX_LENGTH));
  }

  public BDD toBDD(IpWildcard ipWildcard) {
    long ip = ipWildcard.getIp().asLong();
    long wildcard = ipWildcard.getWildcard().asLong();
    BDD acc = _factory.one();
    for (int i = 0; i < Prefix.MAX_PREFIX_LENGTH; i++) {
      boolean significant = !Ip.getBitAtPosition(wildcard, i);
      if (significant) {
        boolean bitValue = Ip.getBitAtPosition(ip, i);
        if (bitValue) {
          acc = acc.and(_bitBDDs[i]);
        } else {
          acc = acc.and(_bitBDDs[i].not());
        }
      }
    }
    return acc;
  }

  @Override
  public BDD visitAclIpSpace(AclIpSpace aclIpSpace) {
    throw new BatfishException("AclIpSpace is unsupported");
  }

  @Override
  public BDD visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    throw new BatfishException("EmptyIpSpace is unsupported");
  }

  @Override
  public BDD visitIpIpSpace(IpIpSpace ipIpSpace) {
    return toBDD(ipIpSpace.getIp());
  }

  @Override
  public BDD visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return toBDD(ipWildcardIpSpace.getIpWildcard());
  }

  @Override
  public BDD visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    throw new BatfishException("IpSpaceReference is unsupported");
  }

  @Override
  public BDD visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    throw new BatfishException("IpWildcardSetIpSpace is unsupported");
  }

  @Override
  public BDD visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return toBDD(prefixIpSpace.getPrefix());
  }

  public BDD toBDD(Prefix prefix) {
    return isRelevantFor(prefix);
  }

  @Override
  public BDD visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return _factory.one();
  }
}
