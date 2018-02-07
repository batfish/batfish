package org.batfish.z3.state;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.StateVisitor;

public class OriginateVrf
    extends State<OriginateVrf, org.batfish.z3.state.OriginateVrf.Parameterization> {

  public static class Parameterization implements StateParameterization<OriginateVrf> {

    private final StateParameter _hostname;

    private final StateParameter _vrf;

    public Parameterization(String hostname, String vrf) {
      _hostname = new StateParameter(hostname, Type.NODE);
      _vrf = new StateParameter(vrf, Type.VRF);
    }

    public StateParameter getHostname() {
      return _hostname;
    }

    @Override
    public String getNodName(String baseName) {
      return String.format("%s_%s_%s", BASE_NAME, _hostname.getId(), _vrf.getId());
    }

    public StateParameter getVrf() {
      return _vrf;
    }
  }

  public static final String BASE_NAME = String.format("S_%s", OriginateVrf.class.getSimpleName());

  private static final Set<Transition<OriginateVrf>> DEFAULT_TRANSITIONS = ImmutableSet.of();

  public static final OriginateVrf INSTANCE = new OriginateVrf();

  public static StateExpr<OriginateVrf, Parameterization> expr(String hostname, String vrf) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname, vrf));
  }

  private OriginateVrf() {
    super(BASE_NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitOriginateVrf(this);
  }

  @Override
  protected Set<Transition<OriginateVrf>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
