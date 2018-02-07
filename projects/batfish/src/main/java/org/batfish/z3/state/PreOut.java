package org.batfish.z3.state;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.IpWildcard;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class PreOut extends State<PreOut, org.batfish.z3.state.PreOut.Parameterization> {

  public static class Parameterization implements StateParameterization<PreOut> {

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

  public static class PostInNotMine implements Transition<PreOut> {

    public static final PostInNotMine INSTANCE = new PostInNotMine();

    private PostInNotMine() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getConfigurations()
          .keySet()
          .stream()
          .filter(Predicates.not(input.getDisabledNodes()::contains))
          .map(
              hostname -> {
                BooleanExpr ipForeignToCurrentNode =
                    new NotExpr(
                        HeaderSpaceMatchExpr.matchDstIp(
                            input
                                .getIpsByHostname()
                                .get(hostname)
                                .stream()
                                .map(IpWildcard::new)
                                .collect(ImmutableSet.toImmutableSet())));
                return new RuleExpr(
                    new IfExpr(
                        new AndExpr(
                            ImmutableList.of(PostIn.expr(hostname), ipForeignToCurrentNode)),
                        expr(hostname)));
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", PreOut.class.getSimpleName());

  private static final Set<Transition<PreOut>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(PostInNotMine.INSTANCE);

  public static final PreOut INSTANCE = new PreOut();

  public static StateExpr<PreOut, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private PreOut() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<PreOut>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
