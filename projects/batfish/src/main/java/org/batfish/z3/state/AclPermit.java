package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.StateVisitor;

public class AclPermit extends State<AclPermit, org.batfish.z3.state.AclPermit.Parameterization> {

  public static class MatchPermitLine implements Transition<AclPermit> {

    public static final MatchPermitLine INSTANCE = new MatchPermitLine();

    private MatchPermitLine() {}

    @Override
    public List<RuleExpr> generate(SynthesizerInput input) {
      return input
          .getEnabledAcls()
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
                            if (lines.get(line).getAction() == LineAction.ACCEPT) {
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

  public static class Parameterization implements StateParameterization<AclPermit> {

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

  public static final String BASE_NAME = String.format("S_%s", AclPermit.class.getSimpleName());

  private static final Set<Transition<AclPermit>> DEFAULT_TRANSITIONS =
      ImmutableSet.of(MatchPermitLine.INSTANCE);

  public static final AclPermit INSTANCE = new AclPermit();

  public static StateExpr<AclPermit, Parameterization> expr(String hostname, String acl) {
    return INSTANCE.buildStateExpr(new Parameterization(hostname, acl));
  }

  private AclPermit() {
    super(BASE_NAME);
  }

  @Override
  public void accept(StateVisitor visitor) {
    visitor.visitAclPermit(this);
  }

  @Override
  protected Set<Transition<AclPermit>> getDefaultTransitions() {
    return DEFAULT_TRANSITIONS;
  }
}
