package org.batfish.z3.state;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class NodeDropAcl
    extends State<NodeDropAcl, org.batfish.z3.state.NodeDropAcl.Parameterization> {

  public static class CopyNodeDropAclIn implements Transition<NodeDropAcl> {

    public static final CopyNodeDropAclIn INSTANCE = new CopyNodeDropAclIn();

    private CopyNodeDropAclIn() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getConfigurations()
          .keySet()
          .stream()
          .filter(Predicates.not(input.getDisabledNodes()::contains))
          .map(hostname -> new RuleExpr(new IfExpr(NodeDropAclIn.expr(hostname), expr(hostname))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class CopyNodeDropAclOut implements Transition<NodeDropAcl> {

    public static final CopyNodeDropAclOut INSTANCE = new CopyNodeDropAclOut();

    private CopyNodeDropAclOut() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getConfigurations()
          .keySet()
          .stream()
          .filter(Predicates.not(input.getDisabledNodes()::contains))
          .map(hostname -> new RuleExpr(new IfExpr(NodeDropAclOut.expr(hostname), expr(hostname))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<NodeDropAcl> {

    private final StateParameter _hostname;

    public Parameterization(String hostname) {
      _hostname = new StateParameter(hostname, Type.NODE);
    }

    public StateParameter getHostname() {
      return _hostname;
    }

    @Override
    public String getNodName(String baseName) {
      return String.format("%s_%s", BASE_NAME, _hostname.getId());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", NodeDropAcl.class.getSimpleName());

  private static final Set<Transition<NodeDropAcl>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(CopyNodeDropAclIn.INSTANCE, CopyNodeDropAclOut.INSTANCE);

  public static final NodeDropAcl INSTANCE = new NodeDropAcl();

  public static StateExpr<NodeDropAcl, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private NodeDropAcl() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<NodeDropAcl>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
