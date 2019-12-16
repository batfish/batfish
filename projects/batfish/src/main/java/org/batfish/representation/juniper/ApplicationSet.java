package org.batfish.representation.juniper;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace.Builder;
import org.batfish.datamodel.LineAction;

public class ApplicationSet implements ApplicationSetMember, Serializable {

  private List<ApplicationSetMemberReference> _members;

  public ApplicationSet() {
    _members = ImmutableList.of();
  }

  @Override
  public void applyTo(
      JuniperConfiguration jc,
      Builder srcHeaderSpaceBuilder,
      LineAction action,
      List<ExprAclLine> lines,
      Warnings w) {
    _members.stream()
        .map(ref -> ref.resolve(jc))
        .filter(Predicates.notNull())
        .forEach(member -> member.applyTo(jc, srcHeaderSpaceBuilder, action, lines, w));
  }

  public List<ApplicationSetMemberReference> getMembers() {
    return _members;
  }

  public void setMembers(List<ApplicationSetMemberReference> members) {
    _members = ImmutableList.copyOf(members);
  }
}
