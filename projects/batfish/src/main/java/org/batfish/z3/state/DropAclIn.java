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

public class DropAclIn extends State<DropAclIn, org.batfish.z3.state.DropAclIn.Parameterization> {

  public static class Parameterization implements StateParameterization<DropAclIn> {
    private static final Parameterization INSTANCE = new Parameterization();

    private Parameterization() {}

    @Override
    public String getNodName(String baseName) {
      return NAME;
    }
  }

  public static class ProjectNodeDropAclIn implements Transition<DropAclIn> {

    public static final ProjectNodeDropAclIn INSTANCE = new ProjectNodeDropAclIn();

    private ProjectNodeDropAclIn() {}

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
                          .map(NodeDropAclIn::expr)
                          .collect(ImmutableList.toImmutableList())),
                  DropAclIn.EXPR)));
    }
  }

  private static final Set<Transition<DropAclIn>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(ProjectNodeDropAclIn.INSTANCE);

  public static final StateExpr<DropAclIn, Parameterization> EXPR;

  public static final DropAclIn INSTANCE;

  public static final String NAME = String.format("S_%s", DropAclIn.class.getSimpleName());

  static {
    INSTANCE = new DropAclIn();
    EXPR = INSTANCE.buildStateExpr(Parameterization.INSTANCE);
  }

  private DropAclIn() {
    super(NAME);
  }

  @Override
  protected Set<Transition<DropAclIn>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
