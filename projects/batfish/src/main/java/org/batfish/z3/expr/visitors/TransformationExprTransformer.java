package org.batfish.z3.expr.visitors;

import static org.batfish.z3.expr.ExtractExpr.newExtractExpr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.DynamicNatRule;
import org.batfish.datamodel.transformation.GenericTransformationRuleVisitor;
import org.batfish.datamodel.transformation.StaticNatRule;
import org.batfish.datamodel.transformation.Transformation.Direction;
import org.batfish.datamodel.transformation.Transformation.RuleAction;
import org.batfish.z3.Field;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.state.AclPermit;

// TODO javadoc
public class TransformationExprTransformer
    implements GenericTransformationRuleVisitor<Entry<AclPermit, BooleanExpr>> {

  private final Direction _direction;
  private final String _hostname;

  public TransformationExprTransformer(Direction direction, String hostname) {
    _direction = direction;
    _hostname = hostname;
  }

  private static BooleanExpr transformedSuffixMatchExpr(Field srcOrDest, int prefixLength) {
    if (prefixLength == Prefix.MAX_PREFIX_LENGTH) {
      return TrueExpr.INSTANCE;
    }
    // prefixLength cannot be 0 for NAT. matchHigh is [0, 30]
    int matchHigh = Prefix.MAX_PREFIX_LENGTH - prefixLength - 1;
    return new EqExpr(
        newExtractExpr(new TransformedVarIntExpr(srcOrDest), 0, matchHigh),
        newExtractExpr(srcOrDest, 0, matchHigh));
  }

  @Override
  public Entry<AclPermit, BooleanExpr> visitStaticTransformationRule(StaticNatRule rule) {
    if (rule.getAction() == RuleAction.DESTINATION_INSIDE) {
      throw new BatfishException("Static NAT with 'inside destination' is not valid");
    }

    Field srcOrDest = null;
    Prefix localOrGlobal = null;

    // inside-to-outside for "source inside"
    if (rule.getAction() == RuleAction.SOURCE_INSIDE && _direction == Direction.EGRESS) {
      // Rewrite source to globalNetwork
      srcOrDest = Field.SRC_IP;
      localOrGlobal = rule.getGlobalNetwork();
    }
    // outside-to-inside for "source inside"
    if (rule.getAction() == RuleAction.SOURCE_INSIDE && _direction == Direction.INGRESS) {
      // Rewrite destination to localNetwork
      srcOrDest = Field.DST_IP;
      localOrGlobal = rule.getLocalNetwork();
    }

    // inside-to-outside for "source outside"
    if (rule.getAction() == RuleAction.SOURCE_OUTSIDE && _direction == Direction.EGRESS) {
      // Rewrite destination to globalNetwork
      srcOrDest = Field.DST_IP;
      localOrGlobal = rule.getGlobalNetwork();
    }
    // outside-to-inside for "source outside"
    if (rule.getAction() == RuleAction.SOURCE_OUTSIDE && _direction == Direction.INGRESS) {
      // Rewrite source to localNetwork
      srcOrDest = Field.SRC_IP;
      localOrGlobal = rule.getLocalNetwork();
    }

    if (srcOrDest == null) {
      throw new BatfishException("Static NAT with unsupported RuleAction");
    }

    // ACL matches source interface for egress NATs and source/dest matches local/global addresses,
    // according to NAT rule
    AclPermit preconditionPreTransformationState =
        rule.getAcl() == null ? null : new AclPermit(_hostname, rule.getAcl().getName());

    BooleanExpr transformationConstraint =
        new AndExpr(
            ImmutableList.of(
                new PrefixMatchExpr(new TransformedVarIntExpr(srcOrDest), localOrGlobal),
                transformedSuffixMatchExpr(srcOrDest, rule.getLocalNetwork().getPrefixLength())));
    return Maps.immutableEntry(preconditionPreTransformationState, transformationConstraint);
  }

  // TODO egress only
  @Nullable
  @Override
  public Entry<AclPermit, BooleanExpr> visitDynamicTransformationRule(DynamicNatRule rule) {
    if (_direction != Direction.EGRESS || rule.getAction() != RuleAction.SOURCE_INSIDE) {
      // TODO Only supports inside-to-outside flow of "source inside" dynamic NAT
      return null;
    }

    IpAccessList acl = rule.getAcl();
    AclPermit preconditionPreTransformationState =
        acl == null ? null : new AclPermit(_hostname, acl.getName());
    BooleanExpr transformationConstraint =
        new RangeMatchExpr(
            new TransformedVarIntExpr(Field.SRC_IP),
            Field.SRC_IP.getSize(),
            ImmutableSet.of(
                Range.closed(rule.getPoolIpFirst().asLong(), rule.getPoolIpLast().asLong())));
    return Maps.immutableEntry(preconditionPreTransformationState, transformationConstraint);
  }
}
