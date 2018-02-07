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

public class PostOutInterface
    extends State<PostOutInterface, org.batfish.z3.state.PostOutInterface.Parameterization> {

  public static class Parameterization implements StateParameterization<PostOutInterface> {

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

  public static class PassOutgoingAcl implements Transition<PostOutInterface> {

    public static final PassOutgoingAcl INSTANCE = new PassOutgoingAcl();

    private PassOutgoingAcl() {}

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
                          String ifaceName = i.getName();
                          String outAcl = i.getOutgoingFilterName();
                          BooleanExpr antecedent;
                          BooleanExpr preOut = PreOutInterface.expr(hostname, ifaceName);
                          if (outAcl != null) {
                            antecedent =
                                new AndExpr(
                                    ImmutableList.of(AclPermit.expr(hostname, outAcl), preOut));
                          } else {
                            antecedent = preOut;
                          }
                          return new RuleExpr(new IfExpr(antecedent, expr(hostname, ifaceName)));
                        });
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME =
      String.format("S_%s", PostOutInterface.class.getSimpleName());

  private static final Set<Transition<PostOutInterface>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(PassOutgoingAcl.INSTANCE);

  public static final PostOutInterface INSTANCE = new PostOutInterface();

  public static StateExpr<PostOutInterface, Parameterization> expr(String hostname, String iface) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname, iface));
  }

  private PostOutInterface() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<PostOutInterface>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
