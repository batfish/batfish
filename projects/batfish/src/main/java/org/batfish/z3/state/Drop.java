package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.visitors.StateVisitor;

public class Drop extends State<Drop, org.batfish.z3.state.Drop.Parameterization> {

  public static class CopyDropNoRoute implements Transition<Drop> {

    public static final CopyDropNoRoute INSTANCE = new CopyDropNoRoute();

    private CopyDropNoRoute() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return ImmutableList.of(new RuleExpr(DropNoRoute.EXPR, Drop.EXPR));
    }
  }

  public static class Parameterization implements StateParameterization<Drop> {
    private static final Parameterization INSTANCE = new Parameterization();

    private Parameterization() {}

    @Override
    public String getNodName(String baseName) {
      return NAME;
    }
  }

  public static class ProjectNodeDrop implements Transition<Drop> {

    public static final ProjectNodeDrop INSTANCE = new ProjectNodeDrop();

    private ProjectNodeDrop() {}

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
                          .map(NodeDrop::expr)
                          .collect(ImmutableList.toImmutableList())),
                  Drop.EXPR)));
    }
  }

  private static final Set<Transition<Drop>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(CopyDropNoRoute.INSTANCE, ProjectNodeDrop.INSTANCE);

  public static final StateExpr<Drop, Parameterization> EXPR;

  public static final Drop INSTANCE;

  public static final String NAME = String.format("S_%s", Drop.class.getSimpleName());

  static {
    INSTANCE = new Drop();
    EXPR = INSTANCE.buildStateExpr(Parameterization.INSTANCE);
  }

  private Drop() {
    super(NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitDrop(this);
  }

  @Override
  protected Set<Transition<Drop>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
