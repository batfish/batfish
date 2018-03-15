package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.IpAddressAcl;
import org.batfish.datamodel.IpAddressAclLine;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.TrueExpr;

/**
 * Transforms an {@link IpSpace} into a {@link BooleanExpr} that is true iff one of the appropriate
 * IP variables is in the space.
 */
public class IpSpaceBooleanExprTransformer implements GenericIpSpaceVisitor<BooleanExpr> {

  private final boolean _useDst;

  private final boolean _useSrc;

  public IpSpaceBooleanExprTransformer(boolean useSrc, boolean useDst) {
    _useDst = useDst;
    _useSrc = useSrc;
  }

  @Override
  public BooleanExpr castToGenericIpSpaceVisitorReturnType(Object o) {
    return (BooleanExpr) o;
  }

  private BooleanExpr matchIpAddressAclLineSpace(IpAddressAclLine line) {
    BooleanExpr matchSpace = line.getIpSpace().accept(this);
    return line.getMatchComplement() ? new NotExpr(matchSpace) : matchSpace;
  }

  @Override
  public BooleanExpr visitIpAddressAcl(IpAddressAcl ipAddressAcl) {
    ImmutableList.Builder<BooleanExpr> lineSpaceMatchConditions = ImmutableList.builder();
    ImmutableList.Builder<BooleanExpr> dontMatchPrevious = ImmutableList.builder();
    ipAddressAcl
        .getLines()
        .forEach(
            line -> {
              BooleanExpr matchCurrentInIsolation = matchIpAddressAclLineSpace(line);
              if (line.getAction() == LineAction.ACCEPT) {
                lineSpaceMatchConditions.add(
                    new AndExpr(
                        ImmutableList.<BooleanExpr>builder()
                            .addAll(dontMatchPrevious.build())
                            .add(matchCurrentInIsolation)
                            .build()));
              }
              dontMatchPrevious.add(new NotExpr(matchCurrentInIsolation));
            });
    return new OrExpr(lineSpaceMatchConditions.build());
  }

  @Override
  public BooleanExpr visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    BooleanExpr matchBlacklist =
        HeaderSpaceMatchExpr.matchIp(ipWildcardSetIpSpace.getBlacklist(), _useSrc, _useDst);
    BooleanExpr matchWhitelist =
        HeaderSpaceMatchExpr.matchIp(ipWildcardSetIpSpace.getWhitelist(), _useSrc, _useDst);
    return new AndExpr(ImmutableList.of(new NotExpr(matchBlacklist), matchWhitelist));
  }

  @Override
  public BooleanExpr visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return TrueExpr.INSTANCE;
  }
}
