package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.StateVisitor;

public class AclLineNoMatch
    extends State<AclLineNoMatch, org.batfish.z3.state.AclLineNoMatch.Parameterization> {

  public static class DontMatchCurrentAndDontMatchPrevious implements Transition<AclLineNoMatch> {

    public static final DontMatchCurrentAndDontMatchPrevious INSTANCE =
        new DontMatchCurrentAndDontMatchPrevious();

    private DontMatchCurrentAndDontMatchPrevious() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getAclConditions()
          .entrySet()
          .stream()
          .flatMap(
              e -> {
                String hostname = e.getKey();
                return e.getValue()
                    .entrySet()
                    .stream()
                    .flatMap(
                        e2 -> {
                          String acl = e2.getKey();
                          return e2.getValue()
                              .entrySet()
                              .stream()
                              .map(
                                  e3 -> {
                                    int line = e3.getKey();
                                    BooleanExpr lineCriteria = new NotExpr(e3.getValue());
                                    BooleanExpr antecedent =
                                        line > 0
                                            ? new AndExpr(
                                                ImmutableList.of(
                                                    lineCriteria, expr(hostname, acl, line - 1)))
                                            : lineCriteria;
                                    return new RuleExpr(
                                        new IfExpr(antecedent, expr(hostname, acl, line)));
                                  });
                        });
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<AclLineNoMatch> {

    private final StateParameter _acl;

    private final StateParameter _hostname;

    private final StateParameter _line;

    public Parameterization(String hostname, String acl, int line) {
      _hostname = new StateParameter(hostname, Type.NODE);
      _acl = new StateParameter(acl, Type.ACL);
      _line = new StateParameter(Integer.toString(line), Type.ACL_LINE);
    }

    public StateParameter getHostname() {
      return _hostname;
    }

    @Override
    public String getNodName(String baseName) {
      return String.format(
          "%s_%s_%s_%s", BASE_NAME, _hostname.getId(), _acl.getId(), _line.getId());
    }
  }

  public static final String BASE_NAME =
      String.format("S_%s", AclLineNoMatch.class.getSimpleName());

  private static final Set<Transition<AclLineNoMatch>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(DontMatchCurrentAndDontMatchPrevious.INSTANCE);

  public static final AclLineNoMatch INSTANCE = new AclLineNoMatch();

  public static StateExpr<AclLineNoMatch, Parameterization> expr(
      String hostname, String acl, int line) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname, acl, line));
  }

  private AclLineNoMatch() {
    super(BASE_NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitAclLineNoMatch(this);
  }

  @Override
  protected Set<Transition<AclLineNoMatch>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
