package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

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
          .getConfigurations()
          .entrySet()
          .stream()
          .filter(e -> !input.getDisabledNodes().contains(e.getKey()))
          .flatMap(
              e -> {
                String hostname = e.getKey();
                Configuration c = e.getValue();
                Set<String> disabledInterfaces = input.getDisabledInterfaces().get(hostname);
                return c.getInterfaces()
                    .keySet()
                    .stream()
                    .filter(
                        ifaceName ->
                            disabledInterfaces == null || !disabledInterfaces.contains(ifaceName))
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
  protected Set<Transition<NodeTransit>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
