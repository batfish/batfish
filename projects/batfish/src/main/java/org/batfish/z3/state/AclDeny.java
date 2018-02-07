package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;

public class AclDeny extends State<AclDeny, org.batfish.z3.state.AclDeny.Parameterization> {

  public static class MatchDenyLine implements Transition<AclDeny> {

    public static final MatchDenyLine INSTANCE = new MatchDenyLine();

    private MatchDenyLine() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getAclMap()
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
                          List<IpAccessListLine> lines = e2.getValue().getLines();
                          ImmutableList.Builder<RuleExpr> denyLineRules = ImmutableList.builder();
                          for (int line = 0; line < lines.size(); line++) {
                            if (lines.get(line).getAction() == LineAction.REJECT) {
                              denyLineRules.add(
                                  new RuleExpr(
                                      new IfExpr(
                                          AclLineMatch.expr(hostname, acl, line),
                                          expr(hostname, acl))));
                            }
                          }
                          return denyLineRules.build().stream();
                        });
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class MatchNoLines implements Transition<AclDeny> {

    public static final MatchNoLines INSTANCE = new MatchNoLines();

    private MatchNoLines() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getAclMap()
          .entrySet()
          .stream()
          .flatMap(
              e -> {
                String hostname = e.getKey();
                return e.getValue()
                    .entrySet()
                    .stream()
                    .map(
                        e2 -> {
                          String acl = e2.getKey();
                          List<IpAccessListLine> lines = e2.getValue().getLines();
                          BooleanExpr ruleInterior;
                          BooleanExpr deny = expr(hostname, acl);
                          if (lines.isEmpty()) {
                            ruleInterior = deny;
                          } else {
                            int lastLine = lines.size() - 1;
                            if (lines.get(lastLine).getAction() == LineAction.ACCEPT) {
                              ruleInterior =
                                  new IfExpr(AclLineNoMatch.expr(hostname, acl, lastLine), deny);
                            } else {
                              return null;
                            }
                          }
                          return new RuleExpr(ruleInterior);
                        })
                    .filter(r -> r != null);
              })
          .collect(ImmutableList.toImmutableList());
    }
  }

  public static class Parameterization implements StateParameterization<AclDeny> {

    private final StateParameter _acl;

    private final StateParameter _hostname;

    public Parameterization(String hostname, String acl) {
      _hostname = new StateParameter(hostname, Type.NODE);
      _acl = new StateParameter(acl, Type.ACL);
    }

    public StateParameter getHostname() {
      return _hostname;
    }

    @Override
    public String getNodName(String baseName) {
      return String.format("%s_%s_%s", BASE_NAME, _hostname.getId(), _acl.getId());
    }
  }

  public static final String BASE_NAME = String.format("S_%s", AclDeny.class.getSimpleName());

  private static final Set<Transition<AclDeny>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(MatchDenyLine.INSTANCE, MatchNoLines.INSTANCE);

  public static final AclDeny INSTANCE = new AclDeny();

  public static StateExpr<AclDeny, Parameterization> expr(String hostname, String acl) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname, acl));
  }

  private AclDeny() {
    super(BASE_NAME);
  }

  @Override
  protected Set<Transition<AclDeny>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
