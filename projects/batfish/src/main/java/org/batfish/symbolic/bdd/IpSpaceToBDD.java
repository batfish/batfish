package org.batfish.symbolic.bdd;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpSpaceToBDD implements GenericIpSpaceVisitor<BDD> {

  private final BDDInteger _bddInteger;

  private final BDDOps _bddOps;

  private final BDDFactory _factory;

  private final Map<String, IpSpace> _namedIpSpaces;

  private final Map<String, BDD> _namedIpSpaceBDDs;

  public IpSpaceToBDD(BDDFactory factory, BDDInteger var) {
    _bddInteger = var;
    _bddOps = new BDDOps(factory);
    _factory = factory;
    _namedIpSpaces = ImmutableMap.of();
    _namedIpSpaceBDDs = ImmutableMap.of();
  }

  public IpSpaceToBDD(BDDFactory factory, BDDInteger var, Map<String, IpSpace> namedIpSpaces) {
    _bddInteger = var;
    _bddOps = new BDDOps(factory);
    _factory = factory;
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _namedIpSpaceBDDs = new HashMap<>();
  }

  @Override
  public BDD castToGenericIpSpaceVisitorReturnType(Object o) {
    return (BDD) o;
  }

  public BDDInteger getBDDInteger() {
    return _bddInteger;
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
    BDD[] bitBDDs = _bddInteger.getBitvec();
    for (int i = 0; i < length; i++) {
      boolean bitValue = Ip.getBitAtPosition(b, i);
      if (bitValue) {
        acc = acc.and(bitBDDs[i]);
      } else {
        acc = acc.and(bitBDDs[i].not());
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
    BDD[] bitBDDs = _bddInteger.getBitvec();
    for (int i = 0; i < Prefix.MAX_PREFIX_LENGTH; i++) {
      boolean significant = !Ip.getBitAtPosition(wildcard, i);
      if (significant) {
        boolean bitValue = Ip.getBitAtPosition(ip, i);
        if (bitValue) {
          acc = acc.and(bitBDDs[i]);
        } else {
          acc = acc.and(bitBDDs[i].not());
        }
      }
    }
    return acc;
  }

  @Override
  public BDD visitAclIpSpace(AclIpSpace aclIpSpace) {
    BDD bdd = _factory.zero();
    for (AclIpSpaceLine aclIpSpaceLine : Lists.reverse(aclIpSpace.getLines())) {
      bdd =
          visit(aclIpSpaceLine.getIpSpace())
              .ite(
                  aclIpSpaceLine.getAction() == LineAction.ACCEPT
                      ? _factory.one()
                      : _factory.zero(),
                  bdd);
    }
    return bdd;
  }

  @Override
  public BDD visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return _factory.zero();
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
    String name = ipSpaceReference.getName();
    Preconditions.checkArgument(
        _namedIpSpaces.containsKey(name), "Undefined IpSpace reference: %s", name);
    return _namedIpSpaceBDDs.computeIfAbsent(name, k -> _namedIpSpaces.get(name).accept(this));
  }

  @Override
  public BDD visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    BDD whitelist =
        _bddOps.or(
            ipWildcardSetIpSpace
                .getWhitelist()
                .stream()
                .map(this::toBDD)
                .collect(Collectors.toList()));

    BDD blacklist =
        _bddOps.or(
            ipWildcardSetIpSpace
                .getBlacklist()
                .stream()
                .map(this::toBDD)
                .collect(Collectors.toList()));

    return whitelist.and(blacklist.not());
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
