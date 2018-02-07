package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class PreInInterface
    extends State<PreInInterface, org.batfish.z3.state.PreInInterface.Parameterization> {

  public static class Parameterization implements StateParameterization<PreInInterface> {

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

  public static class PostOutNeighbor implements Transition<PreInInterface> {

    public static final PostOutNeighbor INSTANCE = new PostOutNeighbor();

    private PostOutNeighbor() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEdges()
          .stream()
          .filter(e -> !input.getFlowSinks().contains(e.getInterface1()))
          .filter(e -> !input.getFlowSinks().contains(e.getInterface2()))
          .map(
              edge ->
                  new RuleExpr(
                      new IfExpr(
                          new AndExpr(
                              ImmutableList.of(
                                  PreOutEdge.expr(edge),
                                  PostOutInterface.expr(edge.getNode1(), edge.getInt1()))),
                          expr(edge.getNode2(), edge.getInt2()))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME =
      String.format("S_%s", PreInInterface.class.getSimpleName());

  private static final Set<Transition<PreInInterface>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(PostOutNeighbor.INSTANCE);

  public static final PreInInterface INSTANCE = new PreInInterface();

  public static StateExpr<PreInInterface, Parameterization> expr(String node, String iface) {
    return INSTANCE.buildStateExpr(new Parameterization(node, iface));
  }

  private PreInInterface() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<PreInInterface>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
