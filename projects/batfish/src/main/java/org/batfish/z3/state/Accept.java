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

public class Accept extends State<Accept, org.batfish.z3.state.Accept.Parameterization> {

  public static class Parameterization implements StateParameterization<Accept> {
    private static final Parameterization INSTANCE = new Parameterization();

    private Parameterization() {}

    @Override
    public String getNodName(String baseName) {
      return NAME;
    }
  }

  public static class ProjectNodeAccept implements Transition<Accept> {

    public static final ProjectNodeAccept INSTANCE = new ProjectNodeAccept();

    private ProjectNodeAccept() {}

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
                          .map(NodeAccept::expr)
                          .collect(ImmutableList.toImmutableList())),
                  Accept.EXPR)));
    }
  }

  private static final Set<Transition<Accept>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(ProjectNodeAccept.INSTANCE);

  public static final StateExpr<Accept, Parameterization> EXPR;

  public static final Accept INSTANCE;

  public static final String NAME = String.format("S_%s", Accept.class.getSimpleName());

  static {
    INSTANCE = new Accept();
    EXPR = INSTANCE.buildStateExpr(Parameterization.INSTANCE);
  }

  private Accept() {
    super(NAME);
  }

  @Override
  protected Set<Transition<Accept>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
