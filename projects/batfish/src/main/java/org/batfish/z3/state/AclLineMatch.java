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

public class AclLineMatch
    extends State<AclLineMatch, org.batfish.z3.state.AclLineMatch.Parameterization> {

  public static class MatchCurrentAndDontMatchPrevious implements Transition<AclLineMatch> {

    public static final MatchCurrentAndDontMatchPrevious INSTANCE =
        new MatchCurrentAndDontMatchPrevious();

    private MatchCurrentAndDontMatchPrevious() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getAclConditions()
          .entrySet()
          .stream()
          .flatMap(
              aclConditionsEntryByNode -> {
                String hostname = aclConditionsEntryByNode.getKey();
                return aclConditionsEntryByNode.getValue()
                    .entrySet()
                    .stream()
                    .flatMap(
                        aclConditionsEntryByAclName -> {
                          String acl = aclConditionsEntryByAclName.getKey();
                          return aclConditionsEntryByAclName.getValue()
                              .entrySet()
                              .stream()
                              .map(
                                  aclConditionsEntryByLine -> {
                                    int line = aclConditionsEntryByLine.getKey();
                                    BooleanExpr lineCriteria = aclConditionsEntryByLine.getValue();
                                    BooleanExpr antecedent =
                                        line > 0
                                            ? new AndExpr(
                                                ImmutableList.of(
                                                    lineCriteria,
                                                    AclLineNoMatch.expr(hostname, acl, line - 1)))
                                            : lineCriteria;
                                    return new RuleExpr(
                                        new IfExpr(antecedent, expr(hostname, acl, line)));
                                  });
                        });
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<AclLineMatch> {

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

  public static final String BASE_NAME = String.format("S_%s", AclLineMatch.class.getSimpleName());

  private static final Set<Transition<AclLineMatch>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(MatchCurrentAndDontMatchPrevious.INSTANCE);

  public static final AclLineMatch INSTANCE = new AclLineMatch();

  public static StateExpr<AclLineMatch, Parameterization> expr(
      String hostname, String aclName, int line) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname, aclName, line));
  }

  private AclLineMatch() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<AclLineMatch>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
