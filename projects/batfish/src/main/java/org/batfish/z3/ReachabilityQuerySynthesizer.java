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
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationVisitor;
import org.batfish.specifier.VrfLocation;
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

    protected List<Location> _ingressLocations;

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

    public T setIngressLocations(List<Location> ingressLocations) {
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

  protected final HeaderSpace _headerSpace;

  protected final List<Location> _ingressLocations;

  protected final Set<String> _nonTransitNodes;

  protected final SrcNattedConstraint _srcNatted;

  protected final Set<String> _transitNodes;

  public ReachabilityQuerySynthesizer(
      @Nonnull HeaderSpace headerSpace,
      @Nonnull List<Location> ingressLocations,
      SrcNattedConstraint srcNatted,
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
        new LocationVisitor<Void>() {
              @Override
              public Void visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation) {
                rules.add(
                    new BasicRuleStatement(
                        initialConstraint,
                        new OriginateInterfaceLink(
                            interfaceLinkLocation.getNodeName(),
                            interfaceLinkLocation.getInterfaceName())));
                return null;
              }

              @Override
              public Void visitInterfaceLocation(InterfaceLocation interfaceLocation) {
                throw new BatfishException("TODO: InterfaceLocation");
              }

              @Override
              public Void visitVrfLocation(VrfLocation vrfLocation) {
                rules.add(
                    new BasicRuleStatement(
                        initialConstraint,
                        new OriginateVrf(vrfLocation.getHostname(), vrfLocation.getVrf())));
                return null;
              }
            }
            ::visit);
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
