package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class PreOutInterface
    extends State<PreOutInterface, org.batfish.z3.state.PreOutInterface.Parameterization> {

  public static class Parameterization implements StateParameterization<PreOutInterface> {

    private final StateParameter _hostname;

    private final StateParameter _interface;

    public Parameterization(String hostname, String iface) {
      _hostname = new StateParameter(hostname, Type.NODE);
      _interface = new StateParameter(iface, Type.INTERFACE);
    }

    public StateParameter getHostname() {
      return _hostname;
    }

    public StateParameter getInterface() {
      return _interface;
    }

    @Override
    public String getNodName(String baseName) {
      return String.format("%s_%s_%s", BASE_NAME, _hostname.getId(), _interface.getId());
    }
  }

  public static class ProjectPreOutEdgeForFlowSinks implements Transition<PreOutInterface> {

    public static final ProjectPreOutEdgeForFlowSinks INSTANCE =
        new ProjectPreOutEdgeForFlowSinks();

    private ProjectPreOutEdgeForFlowSinks() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledFlowSinks()
          .stream()
          .map(
              flowSink ->
                  new RuleExpr(
                      new IfExpr(
                          PreOutEdge.expr(
                              flowSink.getHostname(),
                              flowSink.getInterface(),
                              Configuration.NODE_NONE_NAME,
                              Interface.FLOW_SINK_TERMINATION_NAME),
                          expr(flowSink.getHostname(), flowSink.getInterface()))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class ProjectPreOutEdgeForTopologyEdges implements Transition<PreOutInterface> {

    public static final ProjectPreOutEdgeForTopologyEdges INSTANCE =
        new ProjectPreOutEdgeForTopologyEdges();

    private ProjectPreOutEdgeForTopologyEdges() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledEdges()
          .stream()
          .map(
              edge ->
                  new RuleExpr(
                      new IfExpr(PreOutEdge.expr(edge), expr(edge.getNode1(), edge.getInt1()))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME =
      String.format("S_%s", PreOutInterface.class.getSimpleName());

  private static final Set<Transition<PreOutInterface>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(
          ProjectPreOutEdgeForTopologyEdges.INSTANCE, ProjectPreOutEdgeForFlowSinks.INSTANCE);

  public static final PreOutInterface INSTANCE = new PreOutInterface();

  public static StateExpr<PreOutInterface, Parameterization> expr(String hostname, String iface) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname, iface));
  }

  private PreOutInterface() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<PreOutInterface>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
