package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.StateVisitor;

public class PostIn extends State<PostIn, org.batfish.z3.state.PostIn.Parameterization> {

  public static class CopyOriginate implements Transition<PostIn> {

    public static final CopyOriginate INSTANCE = new CopyOriginate();

    private CopyOriginate() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledNodes()
          .keySet()
          .stream()
          .map(hostname -> new RuleExpr(new IfExpr(Originate.expr(hostname), expr(hostname))))
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<PostIn> {

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

  public static class ProjectPostInInterface implements Transition<PostIn> {

    public static final ProjectPostInInterface INSTANCE = new ProjectPostInInterface();

    private ProjectPostInInterface() {}

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
                    .keySet()
                    .stream()
                    .map(
                        ifaceName ->
                            new RuleExpr(
                                new IfExpr(
                                    PostInInterface.expr(hostname, ifaceName), expr(hostname))));
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", PostIn.class.getSimpleName());

  private static final Set<Transition<PostIn>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(CopyOriginate.INSTANCE, ProjectPostInInterface.INSTANCE);

  public static final PostIn INSTANCE = new PostIn();

  public static StateExpr<PostIn, Parameterization> expr(String hostname) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname));
  }

  private PostIn() {
    super(BASE_NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitPostIn(this);
  }

  @Override
  protected Set<Transition<PostIn>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
