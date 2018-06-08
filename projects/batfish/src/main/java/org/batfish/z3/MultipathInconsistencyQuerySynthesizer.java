package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.question.SrcNattedConstraint;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.Drop;
import org.batfish.z3.state.NeighborUnreachable;
import org.batfish.z3.state.Query;

public class MultipathInconsistencyQuerySynthesizer extends ReachabilityQuerySynthesizer {

  public static class Builder
      extends ReachabilityQuerySynthesizer.Builder<
          MultipathInconsistencyQuerySynthesizer, MultipathInconsistencyQuerySynthesizer.Builder> {

    @Override
    public MultipathInconsistencyQuerySynthesizer build() {
      return new MultipathInconsistencyQuerySynthesizer(
          _headerSpace,
          _srcIpConstraints,
          _srcNatted,
          _requiredTransitNodes,
          _forbiddenTransitNodes);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public Builder setActions(Set<ForwardingAction> actions) {
      return this;
    }

    @Override
    public Builder setFinalNodes(Set<String> finalNodes) {
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private MultipathInconsistencyQuerySynthesizer(
      @Nonnull HeaderSpace headerSpace,
      @Nonnull Map<IngressLocation, BooleanExpr> ingressLocations,
      @Nonnull SrcNattedConstraint srcNatted,
      @Nonnull Set<String> transitNodes,
      @Nonnull Set<String> nonTransitNodes) {
    super(headerSpace, ingressLocations, srcNatted, transitNodes, nonTransitNodes);
  }

  @Override
  public ReachabilityProgram getReachabilityProgram(SynthesizerInput input) {
    ImmutableList.Builder<RuleStatement> rules = ImmutableList.builder();
    addOriginateRules(rules);
    rules.add(
        new BasicRuleStatement(ImmutableSet.of(Accept.INSTANCE, Drop.INSTANCE), Query.INSTANCE));
    rules.add(
        new BasicRuleStatement(
            ImmutableSet.of(Accept.INSTANCE, NeighborUnreachable.INSTANCE), Query.INSTANCE));
    rules.add(
        new BasicRuleStatement(
            ImmutableSet.of(Drop.INSTANCE, NeighborUnreachable.INSTANCE), Query.INSTANCE));
    return ReachabilityProgram.builder()
        .setInput(input)
        .setQueries(ImmutableList.of(new QueryStatement(Query.INSTANCE)))
        .setRules(rules.build())
        .setSmtConstraint(
            new AndExpr(
                ImmutableList.of(
                    new HeaderSpaceMatchExpr(_headerSpace, ImmutableMap.of(), true),
                    getSrcNattedConstraint())))
        .build();
  }
}
