package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.OriginateInterface;
import org.batfish.z3.state.OriginateVrf;

public abstract class ReachabilityQuerySynthesizer extends BaseQuerySynthesizer {

  public abstract static class Builder<
      Q extends ReachabilityQuerySynthesizer, T extends Builder<Q, T>> {
    protected HeaderSpace _headerSpace;

    protected Multimap<String, String> _ingressNodeInterfaces;

    protected Multimap<String, String> _ingressNodeVrfs;

    protected Set<String> _nonTransitNodes;

    protected Boolean _srcNatted;

    protected Set<String> _transitNodes;

    public Builder() {
      _headerSpace = new HeaderSpace();
      _ingressNodeInterfaces = ImmutableMultimap.of();
      _ingressNodeVrfs = ImmutableMultimap.of();
      _nonTransitNodes = ImmutableSet.of();
      _srcNatted = false;
      _transitNodes = ImmutableSet.of();
    }

    public abstract Q build();

    public abstract T getThis();

    public abstract T setActions(Set<ForwardingAction> actions);

    public abstract T setFinalNodes(Set<String> finalNodes);

    public T setHeaderSpace(HeaderSpace headerSpace) {
      _headerSpace = headerSpace;
      return getThis();
    }

    public T setIngressNodeInterfaces(Multimap<String, String> ingressNodeInterfaces) {
      _ingressNodeInterfaces = ImmutableMultimap.copyOf(ingressNodeInterfaces);
      return getThis();
    }

    public T setIngressNodeVrfs(Multimap<String, String> ingressNodeVrfs) {
      _ingressNodeVrfs = ImmutableMultimap.copyOf(ingressNodeVrfs);
      return getThis();
    }

    public T setNonTransitNodes(Set<String> nonTransitNodes) {
      _nonTransitNodes = nonTransitNodes;
      return getThis();
    }

    public T setSrcNatted(Boolean srcNatted) {
      _srcNatted = srcNatted;
      return getThis();
    }

    public T setTransitNodes(Set<String> transitNodes) {
      _transitNodes = transitNodes;
      return getThis();
    }
  }

  protected final HeaderSpace _headerSpace;

  protected final Multimap<String, String> _ingressNodeInterfaces;

  protected final Multimap<String, String> _ingressNodeVrfs;

  protected final Set<String> _nonTransitNodes;

  protected final Boolean _srcNatted;

  protected final Set<String> _transitNodes;

  public ReachabilityQuerySynthesizer(
      @Nonnull HeaderSpace headerSpace,
      @Nonnull Multimap<String, String> ingressNodeInterfaces,
      @Nonnull Multimap<String, String> ingressNodeVrfs,
      Boolean srcNatted,
      @Nonnull Set<String> transitNodes,
      @Nonnull Set<String> nonTransitNodes) {
    _headerSpace = headerSpace;
    _ingressNodeInterfaces = ingressNodeInterfaces;
    _ingressNodeVrfs = ingressNodeVrfs;
    _srcNatted = srcNatted;
    _transitNodes = transitNodes;
    _nonTransitNodes = nonTransitNodes;
  }

  protected final void addOriginateRules(ImmutableList.Builder<RuleStatement> rules) {
    // create rules for injecting symbolic packets into ingress node(s)
    BooleanExpr initialConstraint =
        new AndExpr(
            ImmutableList.of(
                new EqExpr(new VarIntExpr(Field.SRC_IP), new VarIntExpr(Field.ORIG_SRC_IP))));

    _ingressNodeVrfs.forEach(
        (node, vrf) ->
            rules.add(new BasicRuleStatement(initialConstraint, new OriginateVrf(node, vrf))));

    _ingressNodeInterfaces.forEach(
        (node, iface) ->
            rules.add(
                new BasicRuleStatement(initialConstraint, new OriginateInterface(node, iface))));
  }

  protected final BooleanExpr getSrcNattedConstraint() {
    if (_srcNatted != null) {
      BooleanExpr notSrcNatted =
          new EqExpr(new VarIntExpr(Field.SRC_IP), new VarIntExpr(Field.ORIG_SRC_IP));
      if (_srcNatted) {
        return new NotExpr(notSrcNatted);
      } else {
        return notSrcNatted;
      }
    } else {
      return TrueExpr.INSTANCE;
    }
  }
}
