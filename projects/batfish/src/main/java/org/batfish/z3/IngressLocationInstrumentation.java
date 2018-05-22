package org.batfish.z3;

import static java.lang.Math.max;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.GenericStatementVisitor;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.OriginateInterfaceLink;
import org.batfish.z3.state.OriginateVrf;

/**
 * A NOD instrumentation that tracks which of several possible IngressPoints was used to derive each
 * solution.
 */
public class IngressLocationInstrumentation implements GenericStatementVisitor<Statement> {

  private final int _fieldBits;

  private final Field _ingressLocationField;

  private final ImmutableList<IngressLocation> _ingressLocations;

  private final ImmutableMultimap<BooleanExpr, IngressLocation> _ingressLocationsBySrcIpConstraint;

  public static final String INGRESS_LOCATION_FIELD_NAME = "INGRESS_LOCATION";

  public IngressLocationInstrumentation(
      Multimap<BooleanExpr, IngressLocation> ingressLocationsBySrcIpConstraint) {
    _ingressLocations = ImmutableList.copyOf(ingressLocationsBySrcIpConstraint.values());
    _ingressLocationsBySrcIpConstraint =
        ImmutableMultimap.copyOf(ingressLocationsBySrcIpConstraint);
    _fieldBits =
        max(LongMath.log2(_ingressLocationsBySrcIpConstraint.size(), RoundingMode.CEILING), 1);
    _ingressLocationField = new Field(INGRESS_LOCATION_FIELD_NAME, _fieldBits);
  }

  public int getFieldBits() {
    return _fieldBits;
  }

  public List<IngressLocation> getIngressLocations() {
    return _ingressLocations;
  }

  public BooleanExpr getSrcIpConstraint() {
    return new OrExpr(
        _ingressLocationsBySrcIpConstraint
            .asMap()
            .entrySet()
            .stream()
            .map(
                entry -> {
                  BooleanExpr srcIpConstraint = entry.getKey();
                  Collection<IngressLocation> locations = entry.getValue();
                  return new AndExpr(
                      ImmutableList.of(
                          srcIpConstraint,
                          new OrExpr(
                              locations
                                  .stream()
                                  .map(this::getLocationConstraint)
                                  .collect(Collectors.toList()))));
                })
            .collect(Collectors.toList()));
  }

  private BooleanExpr getLocationConstraint(IngressLocation location) {
    int index = _ingressLocations.indexOf(location);
    if (index < 0) {
      return FalseExpr.INSTANCE;
    }
    return new EqExpr(new VarIntExpr(_ingressLocationField), new LitIntExpr(index, _fieldBits));
  }

  public Statement instrumentStatement(Statement statement) {
    return statement.accept(this);
  }

  @Override
  public Statement visitBasicRuleStatement(BasicRuleStatement basicRuleStatement) {
    StateExpr postState = basicRuleStatement.getPostconditionState();
    if (postState instanceof OriginateVrf) {
      OriginateVrf originateVrf = (OriginateVrf) postState;
      IngressLocation location =
          IngressLocation.vrf(originateVrf.getHostname(), originateVrf.getVrf());

      return new BasicRuleStatement(
          new AndExpr(
              ImmutableList.of(
                  basicRuleStatement.getPreconditionStateIndependentConstraints(),
                  getLocationConstraint(location))),
          basicRuleStatement.getPreconditionStates(),
          postState);
    } else if (postState instanceof OriginateInterfaceLink) {
      OriginateInterfaceLink originateInterfaceLink = (OriginateInterfaceLink) postState;
      IngressLocation location =
          IngressLocation.interfaceLink(
              originateInterfaceLink.getHostname(), originateInterfaceLink.getIface());
      return new BasicRuleStatement(
          new AndExpr(
              ImmutableList.of(
                  basicRuleStatement.getPreconditionStateIndependentConstraints(),
                  getLocationConstraint(location))),
          basicRuleStatement.getPreconditionStates(),
          postState);
    } else {
      return basicRuleStatement;
    }
  }

  @Override
  public Statement visitComment(Comment comment) {
    return comment;
  }

  @Override
  public Statement visitQueryStatement(QueryStatement queryStatement) {
    return queryStatement;
  }
}
