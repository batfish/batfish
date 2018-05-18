package org.batfish.z3;

import static org.batfish.main.SrcNattedConstraint.UNCONSTRAINED;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.main.SrcNattedConstraint;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.OriginateInterfaceLink;
import org.batfish.z3.state.OriginateVrf;

public abstract class ReachabilityQuerySynthesizer extends BaseQuerySynthesizer {

  public abstract static class Builder<
      Q extends ReachabilityQuerySynthesizer, T extends Builder<Q, T>> {
    protected Set<String> _forbiddenTransitNodes;

    protected HeaderSpace _headerSpace;

    protected ImmutableList<IngressLocation> _ingressLocations;

    protected Set<String> _requiredTransitNodes;

    protected SrcNattedConstraint _srcNatted;

    public Builder() {
      _forbiddenTransitNodes = ImmutableSet.of();
      _headerSpace = new HeaderSpace();
      _ingressLocations = ImmutableList.of();
      _requiredTransitNodes = ImmutableSet.of();
      _srcNatted = UNCONSTRAINED;
    }

    public abstract Q build();

    public abstract T getThis();

    public abstract T setActions(Set<ForwardingAction> actions);

    public abstract T setFinalNodes(Set<String> finalNodes);

    public T setForbiddenTransitNodes(Set<String> nonTransitNodes) {
      _forbiddenTransitNodes = nonTransitNodes;
      return getThis();
    }

    public T setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
      return getThis();
    }

    public T setIngressLocations(List<IngressLocation> ingressLocations) {
      _ingressLocations = ImmutableList.copyOf(ingressLocations);
      return getThis();
    }

    public T setSrcNatted(SrcNattedConstraint srcNatted) {
      _srcNatted = srcNatted;
      return getThis();
    }

    public T setRequiredTransitNodes(Set<String> transitNodes) {
      _requiredTransitNodes = transitNodes;
      return getThis();
    }
  }

  protected final @Nonnull HeaderSpace _headerSpace;

  protected final @Nonnull List<IngressLocation> _ingressLocations;

  protected final @Nonnull Set<String> _nonTransitNodes;

  protected final @Nonnull SrcNattedConstraint _srcNatted;

  protected final @Nonnull Set<String> _transitNodes;

  public ReachabilityQuerySynthesizer(
      @Nonnull HeaderSpace headerSpace,
      @Nonnull ImmutableList<IngressLocation> ingressLocations,
      @Nonnull SrcNattedConstraint srcNatted,
      @Nonnull Set<String> transitNodes,
      @Nonnull Set<String> nonTransitNodes) {
    _headerSpace = headerSpace;
    _ingressLocations = ImmutableList.copyOf(ingressLocations);
    _srcNatted = srcNatted;
    _transitNodes = ImmutableSet.copyOf(transitNodes);
    _nonTransitNodes = ImmutableSet.copyOf(nonTransitNodes);
  }

  protected final void addOriginateRules(ImmutableList.Builder<RuleStatement> rules) {
    // create rules for injecting symbolic packets into ingress node(s)
    BooleanExpr initialConstraint =
        new EqExpr(new VarIntExpr(Field.SRC_IP), new VarIntExpr(Field.ORIG_SRC_IP));

    _ingressLocations.forEach(
        ingressLocation -> {
          switch (ingressLocation.getType()) {
            case INTERFACE_LINK:
              rules.add(
                  new BasicRuleStatement(
                      initialConstraint,
                      new OriginateInterfaceLink(
                          ingressLocation.getNode(), ingressLocation.getInterface())));
              break;
            case VRF:
              rules.add(
                  new BasicRuleStatement(
                      initialConstraint,
                      new OriginateVrf(ingressLocation.getNode(), ingressLocation.getVrf())));

              break;
            default:
              throw new BatfishException(
                  "Unexpected IngressLocation Type: " + ingressLocation.getType());
          }
        });
  }

  protected final BooleanExpr getSrcNattedConstraint() {
    BooleanExpr notSrcNatted =
        new EqExpr(new VarIntExpr(Field.SRC_IP), new VarIntExpr(Field.ORIG_SRC_IP));
    switch (_srcNatted) {
      case REQUIRE_NOT_SRC_NATTED:
        return notSrcNatted;
      case REQUIRE_SRC_NATTED:
        return new NotExpr(notSrcNatted);
      case UNCONSTRAINED:
        return TrueExpr.INSTANCE;
      default:
        throw new BatfishException("Unexpected SrcNattedConstraint: " + _srcNatted);
    }
  }
}
