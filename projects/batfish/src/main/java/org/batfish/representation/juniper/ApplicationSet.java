package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace.Builder;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public class ApplicationSet implements ApplicationSetMember, Serializable {

  private final boolean _builtIn;
  private List<ApplicationSetMemberReference> _members;
  private String _name;

  public ApplicationSet(String name, boolean builtIn) {
    _name = name;
    _members = ImmutableList.of();
    _builtIn = builtIn;
  }

  @Override
  public void applyTo(
      JuniperConfiguration jc,
      Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<? super ExprAclLine> lines,
      Warnings w) {
    _members.stream()
        .map(ref -> ref.resolve(jc))
        .filter(Predicates.notNull())
        .forEach(member -> member.applyTo(jc, srcHeaderSpaceBuilder, action, lines, w));
  }

  public List<ApplicationSetMemberReference> getMembers() {
    return _members;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Warnings w) {
    return or(
        _members.stream()
            .map(ref -> ref.resolve(jc))
            .filter(Predicates.notNull())
            .map(member -> member.toAclLineMatchExpr(jc, w))
            .collect(ImmutableList.toImmutableList()),
        isBuiltIn()
            ? JunosApplicationSet.getTraceElement(_name)
            : ApplicationSetMember.getTraceElementForUserApplication(
                jc.getFilename(), JuniperStructureType.APPLICATION_SET, _name));
  }

  @Override
  public boolean isBuiltIn() {
    return _builtIn;
  }

  public void setMembers(List<ApplicationSetMemberReference> members) {
    _members = ImmutableList.copyOf(members);
  }
}
