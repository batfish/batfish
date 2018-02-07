package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class PostInInterface
    extends State<PostInInterface, org.batfish.z3.state.PostInInterface.Parameterization> {

  public static class Parameterization implements StateParameterization<PostInInterface> {

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

  public static class PassIncomingAcl implements Transition<PostInInterface> {

    public static final PassIncomingAcl INSTANCE = new PassIncomingAcl();

    private PassIncomingAcl() {}

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
                    .map(
                        i -> {
                          String inAcl = i.getIncomingFilterName();
                          BooleanExpr antecedent;
                          BooleanExpr preIn = PreInInterface.expr(hostname, i.getName());
                          if (inAcl != null) {
                            antecedent =
                                new AndExpr(
                                    ImmutableList.of(AclPermit.expr(hostname, inAcl), preIn));
                          } else {
                            antecedent = preIn;
                          }
                          return new RuleExpr(new IfExpr(antecedent, expr(hostname, i.getName())));
                        });
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME =
      String.format("S_%s", PostInInterface.class.getSimpleName());

  private static final Set<Transition<PostInInterface>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(PassIncomingAcl.INSTANCE);

  public static final PostInInterface INSTANCE = new PostInInterface();

  public static StateExpr<PostInInterface, Parameterization> expr(String hostname, String iface) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname, iface));
  }

  private PostInInterface() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<PostInInterface>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
