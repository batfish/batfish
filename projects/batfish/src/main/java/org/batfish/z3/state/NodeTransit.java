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

public class NodeTransit
    extends State<NodeTransit, org.batfish.z3.state.NodeTransit.Parameterization> {

  public static class Parameterization implements StateParameterization<NodeTransit> {

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

  public static class ProjectPostOutInterface implements Transition<NodeTransit> {

    public static final ProjectPostOutInterface INSTANCE = new ProjectPostOutInterface();

    private ProjectPostOutInterface() {}

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
                    .getEnabledInterfaces()
                    .get(hostname)
                    .keySet()
                    .stream()
                    .map(
                        ifaceName ->
                            new RuleExpr(
                                new IfExpr(
                                    PostOutInterface.expr(hostname, ifaceName), expr(hostname))));
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", NodeTransit.class.getSimpleName());

  private static final Set<Transition<NodeTransit>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(ProjectPostOutInterface.INSTANCE);

  public static final NodeTransit INSTANCE = new NodeTransit();

  public static StateExpr<NodeTransit, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private NodeTransit() {
    super(BASE_NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitNodeTransit(this);
  }

  @Override
  protected Set<Transition<NodeTransit>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
