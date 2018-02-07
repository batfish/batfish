package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.StateVisitor;

public class Originate extends State<Originate, org.batfish.z3.state.Originate.Parameterization> {

  public static class Parameterization implements StateParameterization<Originate> {

    private final StateParameter _hostname;

    public Parameterization(String hostname) {
      _hostname = new StateParameter(hostname, Type.NODE);
    }

    public StateParameter getHostname() {
      return _hostname;
    }

    @Override
    public String getNodName(String baseName) {
      return String.format("%s_%s", BASE_NAME, _hostname.getId());
    }
  }

  public static class ProjectOriginateVrf implements Transition<Originate> {

    public static final ProjectOriginateVrf INSTANCE = new ProjectOriginateVrf();

    private ProjectOriginateVrf() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledNodes()
          .entrySet()
          .stream()
          .flatMap(
              e -> {
                String hostname = e.getKey();
                return input
                    .getEnabledVrfs()
                    .get(hostname)
                    .keySet()
                    .stream()
                    .map(
                        vrfName ->
                            new RuleExpr(
                                new IfExpr(OriginateVrf.expr(hostname, vrfName), expr(hostname))));
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", Originate.class.getSimpleName());

  private static final Set<Transition<Originate>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(ProjectOriginateVrf.INSTANCE);

  public static final Originate INSTANCE = new Originate();

  public static StateExpr<Originate, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private Originate() {
    super(BASE_NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitOriginate(this);
  }

  @Override
  protected Set<Transition<Originate>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
