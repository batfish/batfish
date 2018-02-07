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

public class DropAclOut
    extends State<DropAclOut, org.batfish.z3.state.DropAclOut.Parameterization> {

  public static class Parameterization implements StateParameterization<DropAclOut> {
    private static final Parameterization INSTANCE = new Parameterization();

    private Parameterization() {}

    @Override
    public String getNodName(String baseName) {
      return NAME;
    }
  }

  public static class ProjectNodeDropAclOut implements Transition<DropAclOut> {

    public static final ProjectNodeDropAclOut INSTANCE = new ProjectNodeDropAclOut();

    private ProjectNodeDropAclOut() {}

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
                          .map(NodeDropAclOut::expr)
                          .collect(ImmutableList.toImmutableList())),
                  DropAclOut.EXPR)));
    }
  }

  private static final Set<Transition<DropAclOut>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(ProjectNodeDropAclOut.INSTANCE);

  public static final StateExpr<DropAclOut, Parameterization> EXPR;

  public static final DropAclOut INSTANCE;

  public static final String NAME = String.format("S_%s", DropAclOut.class.getSimpleName());

  static {
    INSTANCE = new DropAclOut();
    EXPR = INSTANCE.buildStateExpr(Parameterization.INSTANCE);
  }

  private DropAclOut() {
    super(NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitDropAclOut(this);
  }

  @Override
  protected Set<Transition<DropAclOut>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
