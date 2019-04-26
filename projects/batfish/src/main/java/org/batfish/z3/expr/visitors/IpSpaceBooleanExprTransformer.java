package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.expr.IpSpaceMatchExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;

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

  private static BooleanExpr matchIp(Ip ip, Field ipField) {
    assert ipField.getSize() == 32;
    return new EqExpr(new LitIntExpr(ip), new VarIntExpr(ipField));
  }

  @Override
  public BooleanExpr visitIpIpSpace(IpIpSpace ipIpSpace) {
    Ip ip = ipIpSpace.getIp();
    return matchAnyField(field -> matchIp(ip, field));
  }

  @Override
  public BooleanExpr visitIpSpaceReference(IpSpaceReference ipSpaceReference) {
    return _namedIpSpaces.get(ipSpaceReference.getName()).accept(this);
  }

  private static BooleanExpr matchPrefix(Prefix prefix, IntExpr ipField) {
    long ip = prefix.getStartIp().asLong();
    int ipWildcardBits = Prefix.MAX_PREFIX_LENGTH - prefix.getPrefixLength();
    int ipStart = ipWildcardBits;
    int ipEnd = Prefix.MAX_PREFIX_LENGTH - 1;
    if (ipStart < Prefix.MAX_PREFIX_LENGTH) {
      IntExpr extractIp = ExtractExpr.newExtractExpr(ipField, ipStart, ipEnd);
      LitIntExpr ipMatchLit = new LitIntExpr(ip, ipStart, ipEnd);
      return new EqExpr(extractIp, ipMatchLit);
    } else {
      return TrueExpr.INSTANCE;
    }
  }

  private static BooleanExpr matchIpWildcard(IpWildcard ipWildcard, IntExpr ipField) {
    if (ipWildcard.isPrefix()) {
      return matchPrefix(ipWildcard.toPrefix(), ipField);
    }

    long ip = ipWildcard.getIp().asLong();
    long wildcard = ipWildcard.getWildcard().asLong();
    ImmutableList.Builder<BooleanExpr> matchIp = ImmutableList.builder();
    for (int currentBitIndex = 0; currentBitIndex < Prefix.MAX_PREFIX_LENGTH; currentBitIndex++) {
      long mask = 1L << currentBitIndex;
      long currentWildcardBit = mask & wildcard;
      boolean useBit = currentWildcardBit == 0;
      if (useBit) {
        IntExpr extractIp = ExtractExpr.newExtractExpr(ipField, currentBitIndex, currentBitIndex);
        LitIntExpr srcIpMatchLit = new LitIntExpr(ip, currentBitIndex, currentBitIndex);
        EqExpr matchIpBit = new EqExpr(extractIp, srcIpMatchLit);
        matchIp.add(matchIpBit);
      }
    }
    return new AndExpr(matchIp.build());
  }

  @Override
  public BooleanExpr visitIpWildcardIpSpace(IpWildcardIpSpace ipWildcardIpSpace) {
    IpWildcard ipWildcard = ipWildcardIpSpace.getIpWildcard();
    return matchAnyField(field -> matchIpWildcard(ipWildcard, new VarIntExpr(field)));
  }

  private static BooleanExpr matchIpWildcards(Set<IpWildcard> ipWildcards, Field ipField) {
    IntExpr intExpr = new VarIntExpr(ipField);
    return new OrExpr(
        ipWildcards.stream()
            .map(ipWildcard -> matchIpWildcard(ipWildcard, intExpr))
            .collect(Collectors.toList()));
  }

  @Override
  public BooleanExpr visitIpWildcardSetIpSpace(IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    return matchAnyField(
        field -> {
          BooleanExpr matchBlacklist = matchIpWildcards(ipWildcardSetIpSpace.getBlacklist(), field);
          BooleanExpr matchWhitelist = matchIpWildcards(ipWildcardSetIpSpace.getWhitelist(), field);
          return new AndExpr(ImmutableList.of(new NotExpr(matchBlacklist), matchWhitelist));
        });
  }

  @Override
  public BooleanExpr visitPrefixIpSpace(PrefixIpSpace prefixIpSpace) {
    Prefix prefix = prefixIpSpace.getPrefix();
    return matchAnyField(field -> matchIpWildcards(ImmutableSet.of(new IpWildcard(prefix)), field));
  }

  private static BooleanExpr matchIpSpace(
      IpSpace ipSpace, Field ipField, Map<String, IpSpace> namedIpSpaces) {
    return ipSpace.accept(new IpSpaceBooleanExprTransformer(namedIpSpaces, ipField));
  }

  public static BooleanExpr matchSrcIp(IpSpace srcIpSpace, Map<String, IpSpace> namedIpSpaces) {
    return matchIpSpace(srcIpSpace, Field.SRC_IP, namedIpSpaces);
  }

  @Override
  public BooleanExpr visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    return TrueExpr.INSTANCE;
  }
}
