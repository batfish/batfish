package org.batfish.z3.state;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.z3.state.visitors.StateVisitor;

public class Debug extends State<Debug, org.batfish.z3.state.Debug.Parameterization> {

  public static class Parameterization implements StateParameterization<Debug> {
    private static final Parameterization INSTANCE = new Parameterization();

    private Parameterization() {}

    @Override
    public String getNodName(String baseName) {
      return NAME;
    }
  }

  private static final Set<Transition<Debug>> DEFAULT_TRANSITIONS = ImmutableSet.of();

  public static final StateExpr<Debug, Parameterization> EXPR;

  public static final Debug INSTANCE;

  public static final String NAME = String.format("S_%s", Debug.class.getSimpleName());

  static {
    INSTANCE = new Debug();
    EXPR = INSTANCE.buildStateExpr(Parameterization.INSTANCE);
  }

  private Debug() {
    super(NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitDebug(this);
  }

  @Override
  protected Set<Transition<Debug>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
