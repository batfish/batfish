package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.IpWildcard;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class NodeAccept
    extends State<NodeAccept, org.batfish.z3.state.NodeAccept.Parameterization> {

  public static class Parameterization implements StateParameterization<NodeAccept> {

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

  public static class PostInForMe implements Transition<NodeAccept> {

    public static final PostInForMe INSTANCE = new PostInForMe();

    private PostInForMe() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledNodes()
          .entrySet()
          .stream()
          .map(
              e ->
                  new RuleExpr(
                      new IfExpr(
                          new AndExpr(
                              ImmutableList.of(
                                  PostIn.expr(e.getKey()),
                                  HeaderSpaceMatchExpr.matchDstIp(
                                      input
                                          .getIpsByHostname()
                                          .get(e.getKey())
                                          .stream()
                                          .map(IpWildcard::new)
                                          .collect(ImmutableSet.toImmutableSet())))),
                          expr(e.getKey()))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class PostOutFlowSinkInterface implements Transition<NodeAccept> {

    public static final PostOutFlowSinkInterface INSTANCE = new PostOutFlowSinkInterface();

    private PostOutFlowSinkInterface() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledFlowSinks()
          .stream()
          .map(
              niPair ->
                  new RuleExpr(
                      new IfExpr(
                          PostOutInterface.expr(niPair.getHostname(), niPair.getInterface()),
                          expr(niPair.getHostname()))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", NodeAccept.class.getSimpleName());

  private static final Set<Transition<NodeAccept>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(PostOutFlowSinkInterface.INSTANCE, PostInForMe.INSTANCE);

  public static final NodeAccept INSTANCE = new NodeAccept();

  public static StateExpr<NodeAccept, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private NodeAccept() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<NodeAccept>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
