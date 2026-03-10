package org.batfish.representation.cisco_asa;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;

public class StandardAccessListServiceSpecifier implements AccessListServiceSpecifier {

  private final Set<Integer> _dscps;

  private final Set<Integer> _ecns;

  public StandardAccessListServiceSpecifier(
      @Nonnull Iterable<Integer> dscps, @Nonnull Iterable<Integer> ecns) {
    _dscps = ImmutableSet.copyOf(dscps);
    _ecns = ImmutableSet.copyOf(ecns);
  }

  public Set<Integer> getDscps() {
    return _dscps;
  }

  public Set<Integer> getEcns() {
    return _ecns;
  }

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ObjectGroup> objectGroups, Map<String, ServiceObject> serviceObjects) {
    return new MatchHeaderSpace(HeaderSpace.builder().setDscps(_dscps).setEcns(_ecns).build());
  }
}
