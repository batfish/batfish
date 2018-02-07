package org.batfish.z3.state;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

public class Query extends State<Query, org.batfish.z3.state.Query.Parameterization> {

  public static class Parameterization implements StateParameterization<Query> {
    private static final Parameterization INSTANCE = new Parameterization();

    private Parameterization() {}

    @Override
    public String getNodName(String baseName) {
      return NAME;
    }
  }

  private static final Set<Transition<Query>> DEFAULT_TRANSITIONS = ImmutableSet.of();

  public static final StateExpr<Query, Parameterization> EXPR;

  public static final Query INSTANCE;

  public static final String NAME = String.format("S_%s", Query.class.getSimpleName());

  static {
    INSTANCE = new Query();
    EXPR = INSTANCE.buildStateExpr(Parameterization.INSTANCE);
  }

  private Query() {
    super(NAME);
  }

  @Override
  protected Set<Transition<Query>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
