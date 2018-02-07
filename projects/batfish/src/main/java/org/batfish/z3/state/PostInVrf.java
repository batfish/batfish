package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class PostInVrf extends State<PostInVrf, org.batfish.z3.state.PostInVrf.Parameterization> {

  public static class CopyOriginateVrf implements Transition<PostInVrf> {

    public static final CopyOriginateVrf INSTANCE = new CopyOriginateVrf();

    private CopyOriginateVrf() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledNodes()
          .entrySet()
          .stream()
          .flatMap(
              e -> {
                String hostname = e.getKey();
                return input
                    .getEnabledVrfs()
                    .get(hostname)
                    .keySet()
                    .stream()
                    .map(
                        vrf ->
                            new RuleExpr(
                                new IfExpr(OriginateVrf.expr(hostname, vrf), expr(hostname, vrf))));
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<PostInVrf> {

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

  public static class PostInInterfaceCorrespondingVrf implements Transition<PostInVrf> {
    public static final PostInInterfaceCorrespondingVrf INSTANCE =
        new PostInInterfaceCorrespondingVrf();

    private PostInInterfaceCorrespondingVrf() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledNodes()
          .entrySet()
          .stream()
          .flatMap(
              e -> {
                String hostname = e.getKey();
                return input
                    .getEnabledInterfaces()
                    .get(hostname)
                    .entrySet()
                    .stream()
                    .map(
                        ei ->
                            new RuleExpr(
                                new IfExpr(
                                    PostInInterface.expr(hostname, ei.getKey()),
                                    expr(hostname, ei.getValue().getVrfName()))));
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", PostInVrf.class.getSimpleName());

  private static final Set<Transition<PostInVrf>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(CopyOriginateVrf.INSTANCE, PostInInterfaceCorrespondingVrf.INSTANCE);

  public static final PostInVrf INSTANCE = new PostInVrf();

  public static StateExpr<PostInVrf, Parameterization> expr(String hostname, String vrfName) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname, vrfName));
  }

  private PostInVrf() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<PostInVrf>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
