package org.batfish.z3.state;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.RuleExpr;

public class DropNoRoute
    extends State<DropNoRoute, org.batfish.z3.state.DropNoRoute.Parameterization> {

  public static class Parameterization implements StateParameterization<DropNoRoute> {
    private static final Parameterization INSTANCE = new Parameterization();

    private Parameterization() {}

    @Override
    public String getNodName(String baseName) {
      return NAME;
    }
  }

  public static class ProjectNodeDropNoRoute implements Transition<DropNoRoute> {

    public static final ProjectNodeDropNoRoute INSTANCE = new ProjectNodeDropNoRoute();

    private ProjectNodeDropNoRoute() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return ImmutableList.of(
          new RuleExpr(
              new IfExpr(
                  new OrExpr(
                      input
                          .getConfigurations()
                          .keySet()
                          .stream()
                          .filter(Predicates.not(input.getDisabledNodes()::contains))
                          .map(NodeDropNoRoute::expr)
                          .collect(ImmutableList.toImmutableList())),
                  DropNoRoute.EXPR)));
    }
  }

  private static final Set<Transition<DropNoRoute>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(ProjectNodeDropNoRoute.INSTANCE);

  public static final StateExpr<DropNoRoute, Parameterization> EXPR;

  public static final DropNoRoute INSTANCE;

  public static final String NAME = String.format("S_%s", Drop.class.getSimpleName());

  static {
    INSTANCE = new DropNoRoute();
    EXPR = INSTANCE.buildStateExpr(Parameterization.INSTANCE);
  }

  private DropNoRoute() {
    super(NAME);
  }

  @Override
  protected Set<Transition<DropNoRoute>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
