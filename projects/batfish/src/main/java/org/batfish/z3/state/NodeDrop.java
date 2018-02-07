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

public class NodeDrop extends State<NodeDrop, org.batfish.z3.state.NodeDrop.Parameterization> {

  public static class CopyNodeDropAcl implements Transition<NodeDrop> {

    public static final CopyNodeDropAcl INSTANCE = new CopyNodeDropAcl();

    private CopyNodeDropAcl() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getConfigurations()
          .keySet()
          .stream()
          .filter(Predicates.not(input.getDisabledNodes()::contains))
          .map(hostname -> new RuleExpr(new IfExpr(NodeDropAcl.expr(hostname), expr(hostname))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class CopyNodeDropNoRoute implements Transition<NodeDrop> {

    public static final CopyNodeDropNoRoute INSTANCE = new CopyNodeDropNoRoute();

    private CopyNodeDropNoRoute() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getConfigurations()
          .keySet()
          .stream()
          .filter(Predicates.not(input.getDisabledNodes()::contains))
          .map(hostname -> new RuleExpr(new IfExpr(NodeDropNoRoute.expr(hostname), expr(hostname))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class CopyNodeDropNullRoute implements Transition<NodeDrop> {

    public static final CopyNodeDropNullRoute INSTANCE = new CopyNodeDropNullRoute();

    private CopyNodeDropNullRoute() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getConfigurations()
          .keySet()
          .stream()
          .filter(Predicates.not(input.getDisabledNodes()::contains))
          .map(
              hostname ->
                  new RuleExpr(new IfExpr(NodeDropNullRoute.expr(hostname), expr(hostname))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<NodeDrop> {

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

  public static final String BASE_NAME = String.format("S_%s", NodeDrop.class.getSimpleName());

  private static final Set<Transition<NodeDrop>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(
          CopyNodeDropAcl.INSTANCE, CopyNodeDropNoRoute.INSTANCE, CopyNodeDropNullRoute.INSTANCE);

  public static final NodeDrop INSTANCE = new NodeDrop();

  public static StateExpr<NodeDrop, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private NodeDrop() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<NodeDrop>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
