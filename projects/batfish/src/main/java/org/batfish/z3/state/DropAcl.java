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

public class DropAcl extends State<DropAcl, org.batfish.z3.state.DropAcl.Parameterization> {

  public static class CopyDropAclIn implements Transition<DropAcl> {

    public static final CopyDropAclIn INSTANCE = new CopyDropAclIn();

    private CopyDropAclIn() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return ImmutableList.of(new RuleExpr(DropAclIn.EXPR, DropAcl.EXPR));
    }
  }

  public static class CopyDropAclOut implements Transition<DropAcl> {

    public static final CopyDropAclOut INSTANCE = new CopyDropAclOut();

    private CopyDropAclOut() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return ImmutableList.of(new RuleExpr(DropAclOut.EXPR, DropAcl.EXPR));
    }
  }

  public static class Parameterization implements StateParameterization<DropAcl> {
    private static final Parameterization INSTANCE = new Parameterization();

    private Parameterization() {}

    @Override
    public String getNodName(String baseName) {
      return NAME;
    }
  }

  /** Use instead of {@link CopyDropAclIn} and {@link CopyDropAclOut} when they are disabled */
  public static class ProjectNodeDropAcl implements Transition<DropAcl> {

    public static final ProjectNodeDropAcl INSTANCE = new ProjectNodeDropAcl();

    private ProjectNodeDropAcl() {}

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
                          .map(NodeDropAcl::expr)
                          .collect(ImmutableList.toImmutableList())),
                  DropAcl.EXPR)));
    }
  }

  private static final Set<Transition<DropAcl>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(CopyDropAclIn.INSTANCE, CopyDropAclOut.INSTANCE);

  public static final StateExpr<DropAcl, Parameterization> EXPR;

  public static final DropAcl INSTANCE;

  public static final String NAME = String.format("S_%s", DropAcl.class.getSimpleName());

  static {
    INSTANCE = new DropAcl();
    EXPR = INSTANCE.buildStateExpr(Parameterization.INSTANCE);
  }

  private DropAcl() {
    super(NAME);
  }

  @Override
  protected Set<Transition<DropAcl>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
