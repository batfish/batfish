package org.batfish.z3.state;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.z3.state.StateParameter.Type;

public class NumberedQuery
    extends State<NumberedQuery, org.batfish.z3.state.NumberedQuery.Parameterization> {

  public static class Parameterization implements StateParameterization<NumberedQuery> {

    private final StateParameter _queryNumber;

    public Parameterization(int queryNumber) {
      _queryNumber = new StateParameter(Integer.toString(queryNumber), Type.QUERY_NUMBER);
    }

    public StateParameter getHostname() {
      return _queryNumber;
    }

    @Override
    public String getNodName(String baseName) {
      return String.format("%s_%s", BASE_NAME, _queryNumber.getId());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", NumberedQuery.class.getSimpleName());

  private static final Set<Transition<NumberedQuery>> DEFAULT_TRANSITIONS = ImmutableSet.of();

  public static final NumberedQuery INSTANCE = new NumberedQuery();

  public static StateExpr<NumberedQuery, Parameterization> expr(int line) {
    return INSTANCE.buildStateExpr(new Parameterization(line));
  }

  private NumberedQuery() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<NumberedQuery>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
