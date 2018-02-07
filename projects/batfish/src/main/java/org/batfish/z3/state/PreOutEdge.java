package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class PreOutEdge
    extends State<PreOutEdge, org.batfish.z3.state.PreOutEdge.Parameterization> {

  public static class DestinationRouting implements Transition<PreOutEdge> {

    public static final DestinationRouting INSTANCE = new DestinationRouting();

    private static boolean isLoopbackInterface(String ifaceName) {
      String lcIfaceName = ifaceName.toLowerCase();
      return lcIfaceName.startsWith("lo");
    }

    private DestinationRouting() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getConfigurations()
          .entrySet()
          .stream()
          .filter(e -> !input.getDisabledNodes().contains(e.getKey()))
          .filter(e -> input.getFibs().containsKey(e.getKey()))
          .flatMap(
              e -> {
                String hostname = e.getKey();
                Configuration c = e.getValue();
                Set<String> disabledVrfs = input.getDisabledVrfs().get(hostname);
                Map<String, Map<String, Map<NodeInterfacePair, BooleanExpr>>> fibConditionsByVrf =
                    input.getFibConditions().get(hostname);
                return c.getVrfs()
                    .keySet()
                    .stream()
                    .filter(vrfName -> disabledVrfs == null || !disabledVrfs.contains(vrfName))
                    .filter(input.getFibs().get(hostname)::containsKey)
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
                                    return !isLoopbackInterface(outInterface)
                                        && !CommonUtil.isNullInterface(outInterface)
                                        && !outInterface.equals(FibRow.DROP_INTERFACE);
                                  })
                              .flatMap(
                                  e2 -> {
                                    String outInterface = e2.getKey();
                                    Map<NodeInterfacePair, BooleanExpr> fibConditionsByReceiver =
                                        fibConditionsByInterface.get(outInterface);
                                    return fibConditionsByReceiver
                                        .entrySet()
                                        .stream()
                                        .map(
                                            e3 -> {
                                              NodeInterfacePair receiver = e3.getKey();
                                              BooleanExpr conditions = e3.getValue();
                                              String inNode = receiver.getHostname();
                                              String inInterface = receiver.getInterface();
                                              return new RuleExpr(
                                                  new IfExpr(
                                                      conditions,
                                                      expr(
                                                          hostname,
                                                          outInterface,
                                                          inNode,
                                                          inInterface)));
                                            });
                                  });
                        });
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<PreOutEdge> {

    private final StateParameter _dstInterface;

    private final StateParameter _dstNode;

    private final StateParameter _srcInterface;

    private final StateParameter _srcNode;

    public Parameterization(Edge edge) {
      this(edge.getNode1(), edge.getInt1(), edge.getNode2(), edge.getInt2());
    }

    public Parameterization(
        String srcNode, String srcInterface, String dstNode, String dstInterface) {
      _srcNode = new StateParameter(srcNode, Type.NODE);
      _srcInterface = new StateParameter(srcInterface, Type.INTERFACE);
      _dstNode = new StateParameter(dstNode, Type.NODE);
      _dstInterface = new StateParameter(dstInterface, Type.INTERFACE);
    }

    public StateParameter getHostname() {
      return _srcNode;
    }

    public StateParameter getInterface() {
      return _srcInterface;
    }

    @Override
    public String getNodName(String baseName) {
      return String.format(
          "%s_%s_%s_%s_%s",
          BASE_NAME,
          _srcNode.getId(),
          _srcInterface.getId(),
          _dstNode.getId(),
          _dstInterface.getId());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", PreOutEdge.class.getSimpleName());

  private static final Set<Transition<PreOutEdge>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(DestinationRouting.INSTANCE);

  public static final PreOutEdge INSTANCE = new PreOutEdge();

  public static StateExpr<PreOutEdge, Parameterization> expr(Edge edge) {
    return INSTANCE.buildStateExpr(new Parameterization(edge));
  }

  public static StateExpr<PreOutEdge, Parameterization> expr(
      String node1, String interface1, String node2, String interface2) {
    return INSTANCE.buildStateExpr(new Parameterization(node1, interface1, node2, interface2));
  }

  private PreOutEdge() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<PreOutEdge>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
