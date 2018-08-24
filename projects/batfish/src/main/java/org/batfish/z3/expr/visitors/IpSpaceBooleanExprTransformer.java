package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import org.batfish.z3.Field;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.IpSpaceMatchExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.TrueExpr;

/**
 * Transforms an {@link IpSpace} into a {@link BooleanExpr} that is true iff one of the appropriate
 * IP variables is in the space.
 */
public class IpSpaceBooleanExprTransformer implements GenericIpSpaceVisitor<BooleanExpr> {

  private final List<Field> _fields;

  private Map<String, IpSpace> _namedIpSpaces;

  public IpSpaceBooleanExprTransformer(Map<String, IpSpace> namedIpSpaces, Field... fields) {
    _fields = ImmutableList.copyOf(fields);
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
  }

  @Override
  public BooleanExpr castToGenericIpSpaceVisitorReturnType(Object o) {
    return (BooleanExpr) o;
  }

  private BooleanExpr matchAnyField(Function<Field, BooleanExpr> matchOne) {
    return new OrExpr(_fields.stream().map(matchOne).collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BooleanExpr visitAclIpSpace(AclIpSpace aclIpSpace) {
    return matchAnyField(
        field -> {
          // right fold
          BooleanExpr expr = FalseExpr.INSTANCE;
          for (int i = aclIpSpace.getLines().size() - 1; i >= 0; i--) {
            AclIpSpaceLine line = aclIpSpace.getLines().get(i);
            expr =
                new IfThenElse(
                    new IpSpaceMatchExpr(line.getIpSpace(), _namedIpSpaces, field).getExpr(),
                    line.getAction() == LineAction.PERMIT ? TrueExpr.INSTANCE : FalseExpr.INSTANCE,
                    expr);
          }
          return expr;
        });
  }

  @Override
  public BooleanExpr visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return FalseExpr.INSTANCE;
  }

  @Override
  public BooleanExpr visitIpIpSpace(IpIpSpace ipIpSpace) {
    Ip ip = ipIpSpace.getIp();
    return matchAnyField(field -> HeaderSpaceMatchExpr.matchIp(ip, field));
  }

  @Override
  public BooleanExpr visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    return _namedIpSpaces.get(ipSpaceReference.getName()).accept(this);
  }

  @Override
  public BooleanExpr visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    IpWildcard ipWildcard = ipWildcardIpSpace.getIpWildcard();
    return matchAnyField(field -> HeaderSpaceMatchExpr.matchIpWildcard(ipWildcard, field));
  }

  @Override
  public BooleanExpr visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return matchAnyField(
        field -> {
          BooleanExpr matchBlacklist =
              HeaderSpaceMatchExpr.matchIpWildcards(ipWildcardSetIpSpace.getBlacklist(), field);
          BooleanExpr matchWhitelist =
              HeaderSpaceMatchExpr.matchIpWildcards(ipWildcardSetIpSpace.getWhitelist(), field);
          return new AndExpr(ImmutableList.of(new NotExpr(matchBlacklist), matchWhitelist));
        });
  }

  @Override
  public BooleanExpr visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    Prefix prefix = prefixIpSpace.getPrefix();
    return matchAnyField(
        field ->
            HeaderSpaceMatchExpr.matchIpWildcards(ImmutableSet.of(new IpWildcard(prefix)), field));
  }

  @Override
  public BooleanExpr visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return TrueExpr.INSTANCE;
  }
}
