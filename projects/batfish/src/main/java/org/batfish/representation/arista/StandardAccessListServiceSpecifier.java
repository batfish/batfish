package org.batfish.representation.arista;

import com.google.common.collect.ImmutableSet;
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
  @Nonnull
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new MatchHeaderSpace(HeaderSpace.builder().setDscps(_dscps).setEcns(_ecns).build());
  }
}
