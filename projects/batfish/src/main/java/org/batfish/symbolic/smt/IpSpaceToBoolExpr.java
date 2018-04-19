package org.batfish.symbolic.smt;

import com.google.common.collect.Lists;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclIpSpaceLine;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardIpSpace;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PrefixIpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpSpaceToBoolExpr implements GenericIpSpaceVisitor<BoolExpr> {
  private final Encoder _encoder;

  private final BitVecExpr _var;

  public IpSpaceToBoolExpr(Encoder encoder, BitVecExpr var) {
    _encoder = encoder;
    _var = var;
  }

  @Override
  public BoolExpr castToGenericIpSpaceVisitorReturnType(Object o) {
    return (BoolExpr) o;
  }

  @Override
  public BoolExpr visitAclIpSpace(AclIpSpace aclIpSpace) {
    BoolExpr expr = _encoder.mkFalse();
    for (AclIpSpaceLine aclIpSpaceLine : Lists.reverse(aclIpSpace.getLines())) {
      BoolExpr matchExpr = aclIpSpaceLine.getIpSpace().accept(this);
      BoolExpr actionExpr =
          aclIpSpaceLine.getAction() == LineAction.ACCEPT ? _encoder.mkTrue() : _encoder.mkFalse();
      expr = _encoder.mkIf(matchExpr, actionExpr, expr);
    }
    return expr;
  }

  @Override
  public BoolExpr visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return _encoder.mkFalse();
  }

  @Override
  public BoolExpr visitIpIpSpace(IpIpSpace ipIpSpace) {
    return _encoder.mkEq(_var, _encoder.mkBV(ipIpSpace.getIp().asLong(), 32));
  }

  @Override
  public BoolExpr visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    return toBoolExpr(ipWildcardIpSpace.getIpWildcard());
  }

  private BoolExpr toBoolExpr(IpWildcard ipWildcard) {
    BitVecExpr ip = _encoder.mkBV(ipWildcard.getIp().asLong(), 32);
    BitVecExpr mask = _encoder.mkBV(~ipWildcard.getWildcard().asLong(), 32);
    return _encoder.mkEq(_encoder.mkBVAND(_var, mask), _encoder.mkBVAND(ip, mask));
  }

  @Override
  public BoolExpr visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    BoolExpr whitelistExpr =
        _encoder.mkOr(
            ipWildcardSetIpSpace
                .getWhitelist()
                .stream()
                .map(this::toBoolExpr)
                .toArray(BoolExpr[]::new));

    BoolExpr blacklistExpr =
        _encoder.mkOr(
            ipWildcardSetIpSpace
                .getBlacklist()
                .stream()
                .map(this::toBoolExpr)
                .toArray(BoolExpr[]::new));

    return _encoder.mkAnd(whitelistExpr, _encoder.mkNot(blacklistExpr));
  }

  @Override
  public BoolExpr visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    return toBoolExpr(new IpWildcard(prefixIpSpace.getPrefix()));
  }

  @Override
  public BoolExpr visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return _encoder.mkTrue();
  }

  @Override
  public BoolExpr visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
