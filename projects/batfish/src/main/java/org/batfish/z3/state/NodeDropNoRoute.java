package org.batfish.z3.state;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class NodeDropNoRoute
    extends State<NodeDropNoRoute, org.batfish.z3.state.NodeDropNoRoute.Parameterization> {

  public static class DestinationRouting implements Transition<NodeDropNoRoute> {

    public static final DestinationRouting INSTANCE = new DestinationRouting();

    private DestinationRouting() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledNodes()
          .entrySet()
          .stream()
          .filter(e -> input.getFibs().containsKey(e.getKey()))
          .flatMap(
              e -> {
                String hostname = e.getKey();
                Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>> fibConditionsByVrf =
                    input.getFibConditions().get(hostname);
                return input
                    .getEnabledVrfs()
                    .get(hostname)
                    .keySet()
                    .stream()
                    .filter(Predicates.not(input.getFibs().get(hostname)::containsKey))
                    .flatMap(
                        vrfName -> {
                          Map<String, Map<NodeInterfacePair, BooleanExpr>>
                              fibConditionsByInterface = fibConditionsByVrf.get(vrfName);
                          return fibConditionsByInterface
                              .entrySet()
                              .stream()
                              .filter(e2 -> e2.getKey().equals(FibRow.DROP_NO_ROUTE))
                              .map(
                                  e2 -> {
                                    String outInterface = e2.getKey();
                                    BooleanExpr conditions =
                                        fibConditionsByInterface
                                            .get(outInterface)
                                            .get(NodeInterfacePair.NONE);
                                    return new RuleExpr(new IfExpr(conditions, expr(hostname)));
                                  });
                        });
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<NodeDropNoRoute> {

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

  public static final String BASE_NAME = String.format("S_%s", NodeDrop.class.getSimpleName());

  private static final Set<Transition<NodeDropNoRoute>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(DestinationRouting.INSTANCE);

  public static final NodeDropNoRoute INSTANCE = new NodeDropNoRoute();

  public static StateExpr<NodeDropNoRoute, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private NodeDropNoRoute() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<NodeDropNoRoute>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
