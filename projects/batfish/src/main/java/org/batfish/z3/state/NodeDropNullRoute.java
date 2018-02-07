package org.batfish.z3.state;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.StateVisitor;

public class NodeDropNullRoute
    extends State<NodeDropNullRoute, org.batfish.z3.state.NodeDropNullRoute.Parameterization> {

  public static class DestinationRouting implements Transition<NodeDropNullRoute> {

    public static final DestinationRouting INSTANCE = new DestinationRouting();

    private static boolean isLoopbackInterface(String ifaceName) {
      String lcIfaceName = ifaceName.toLowerCase();
      return lcIfaceName.startsWith("lo");
    }

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
                              .filter(
                                  e2 -> {
                                    String outInterface = e2.getKey();
                                    return isLoopbackInterface(outInterface)
                                        || CommonUtil.isNullInterface(outInterface);
                                  })
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

  public static class Parameterization implements StateParameterization<NodeDropNullRoute> {

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

  private static final Set<Transition<NodeDropNullRoute>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(DestinationRouting.INSTANCE);

  public static final NodeDropNullRoute INSTANCE = new NodeDropNullRoute();

  public static StateExpr<NodeDropNullRoute, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private NodeDropNullRoute() {
    super(BASE_NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitNodeDropNullRoute(this);
  }

  @Override
  protected Set<Transition<NodeDropNullRoute>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
