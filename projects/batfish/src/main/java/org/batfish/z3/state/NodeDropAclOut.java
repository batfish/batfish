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

public class NodeDropAclOut
    extends State<NodeDropAclOut, org.batfish.z3.state.NodeDropAclOut.Parameterization> {

  public static class FailOutgoingAcl implements Transition<NodeDropAclOut> {

    public static final FailOutgoingAcl INSTANCE = new FailOutgoingAcl();

    private FailOutgoingAcl() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getTopologyInterfaces()
          .entrySet()
          .stream()
          .flatMap(
              e -> {
                String hostname = e.getKey();
                return e.getValue()
                    .stream()
                    .filter(i -> i.getOutgoingFilterName() != null)
                    .map(
                        i -> {
                          String outAcl = i.getOutgoingFilterName();
                          return new RuleExpr(
                              new IfExpr(
                                  new AndExpr(
                                      ImmutableList.of(
                                          AclDeny.expr(hostname, outAcl),
                                          PreOutInterface.expr(hostname, i.getName()))),
                                  expr(hostname)));
                        });
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<NodeDropAclOut> {

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

  public static final String BASE_NAME =
      String.format("S_%s", NodeDropAclOut.class.getSimpleName());

  private static final Set<Transition<NodeDropAclOut>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(FailOutgoingAcl.INSTANCE);

  public static final NodeDropAclOut INSTANCE = new NodeDropAclOut();

  public static StateExpr<NodeDropAclOut, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private NodeDropAclOut() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<NodeDropAclOut>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
