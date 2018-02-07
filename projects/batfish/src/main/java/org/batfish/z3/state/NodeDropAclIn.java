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

public class NodeDropAclIn
    extends State<NodeDropAclIn, org.batfish.z3.state.NodeDropAclIn.Parameterization> {

  public static class FailIncomingAcl implements Transition<NodeDropAclIn> {

    public static final FailIncomingAcl INSTANCE = new FailIncomingAcl();

    private FailIncomingAcl() {}

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
                    .filter(i -> i.getIncomingFilterName() != null)
                    .map(
                        i -> {
                          String inAcl = i.getIncomingFilterName();
                          return new RuleExpr(
                              new IfExpr(
                                  new AndExpr(
                                      ImmutableList.of(
                                          AclDeny.expr(hostname, inAcl),
                                          PreInInterface.expr(hostname, i.getName()))),
                                  expr(hostname)));
                        });
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<NodeDropAclIn> {

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

  public static final String BASE_NAME = String.format("S_%s", NodeDropAclIn.class.getSimpleName());

  private static final Set<Transition<NodeDropAclIn>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(FailIncomingAcl.INSTANCE);

  public static final NodeDropAclIn INSTANCE = new NodeDropAclIn();

  public static StateExpr<NodeDropAclIn, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private NodeDropAclIn() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<NodeDropAclIn>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
