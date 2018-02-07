package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.RuleExpr;

public class DropNullRoute
    extends State<DropNullRoute, org.batfish.z3.state.DropNullRoute.Parameterization> {

  public static class Parameterization implements StateParameterization<DropNullRoute> {
    private static final Parameterization INSTANCE = new Parameterization();

    private Parameterization() {}

    @Override
    public String getNodName(String baseName) {
      return NAME;
    }
  }

  public static class ProjectNodeDropNullRoute implements Transition<DropNullRoute> {

    public static final ProjectNodeDropNullRoute INSTANCE = new ProjectNodeDropNullRoute();

    private ProjectNodeDropNullRoute() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return ImmutableList.of(
          new RuleExpr(
              new IfExpr(
                  new OrExpr(
                      input
                          .getEnabledNodes()
                          .keySet()
                          .stream()
                          .map(NodeDropNullRoute::expr)
                          .collect(ImmutableList.toImmutableList())),
                  DropNullRoute.EXPR)));
    }
  }

  private static final Set<Transition<DropNullRoute>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(ProjectNodeDropNullRoute.INSTANCE);

  public static final StateExpr<DropNullRoute, Parameterization> EXPR;

  public static final DropNullRoute INSTANCE;

  public static final String NAME = String.format("S_%s", Drop.class.getSimpleName());

  static {
    INSTANCE = new DropNullRoute();
    EXPR = INSTANCE.buildStateExpr(Parameterization.INSTANCE);
  }

  private DropNullRoute() {
    super(NAME);
  }

  @Override
  protected Set<Transition<DropNullRoute>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
