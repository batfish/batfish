package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
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
import org.batfish.z3.state.OriginateVrf;

public abstract class ReachabilityQuerySynthesizer extends BaseQuerySynthesizer {

  public abstract static class Builder<
      Q extends ReachabilityQuerySynthesizer, T extends Builder<Q, T>> {
    protected HeaderSpace _headerSpace;

    protected Map<String, Set<String>> _ingressNodeVrfs;

    protected Set<String> _nonTransitNodes;

    protected Boolean _srcNatted;

    protected Set<String> _transitNodes;

    public Builder() {
      _headerSpace = new HeaderSpace();
      _ingressNodeVrfs = ImmutableMap.of();
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

    public T setIngressNodeVrfs(Map<String, Set<String>> ingressNodeVrfs) {
      _ingressNodeVrfs = ingressNodeVrfs;
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

  protected final Map<String, Set<String>> _ingressNodeVrfs;

  protected final Set<String> _nonTransitNodes;

  protected final Boolean _srcNatted;

  protected final Set<String> _transitNodes;

  public ReachabilityQuerySynthesizer(
      @Nonnull HeaderSpace headerSpace,
      @Nonnull Map<String, Set<String>> ingressNodeVrfs,
      Boolean srcNatted,
      @Nonnull Set<String> transitNodes,
      @Nonnull Set<String> nonTransitNodes) {
    _headerSpace = headerSpace;
    _ingressNodeVrfs = ingressNodeVrfs;
    _srcNatted = srcNatted;
    _transitNodes = transitNodes;
    _nonTransitNodes = nonTransitNodes;
  }

  protected final void addOriginateRules(ImmutableList.Builder<RuleStatement> rules) {
    // create rules for injecting symbolic packets into ingress node(s)
    for (String ingressNode : _ingressNodeVrfs.keySet()) {
      for (String ingressVrf : _ingressNodeVrfs.get(ingressNode)) {
        rules.add(
            new BasicRuleStatement(
                new AndExpr(
                    ImmutableList.of(
                        new EqExpr(
                            new VarIntExpr(Field.SRC_IP), new VarIntExpr(Field.ORIG_SRC_IP)))),
                new OriginateVrf(ingressNode, ingressVrf)));
      }
    }
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
