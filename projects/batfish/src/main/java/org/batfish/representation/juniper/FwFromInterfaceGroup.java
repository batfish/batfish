package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.representation.juniper.FwTerm.Field;

/** Matches traffic received on an interface assigned to a numbered interface group. */
@ParametersAreNonnullByDefault
public final class FwFromInterfaceGroup implements FwFrom {

  public FwFromInterfaceGroup(SubRange group, boolean except, Family family) {
    _except = except;
    _family = family;
    _group = group;
  }

  public boolean getExcept() {
    return _except;
  }

  public @Nonnull Family getFamily() {
    return _family;
  }

  @Override
  public Field getField() {
    return Field.SOURCE_INTERFACE;
  }

  public @Nonnull SubRange getGroup() {
    return _group;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    ImmutableSet.Builder<String> matchingInterfaces = ImmutableSet.builder();
    for (Interface iface : jc.getMasterLogicalSystem().getInterfaces().values()) {
      addIfMatching(iface, matchingInterfaces);
      for (Interface unit : iface.getUnits().values()) {
        addIfMatching(unit, matchingInterfaces);
      }
    }
    ImmutableSet<String> interfaces = matchingInterfaces.build();
    AclLineMatchExpr match =
        interfaces.isEmpty()
            ? FALSE
            : new MatchSrcInterface(
                interfaces,
                TraceElement.of(
                    String.format(
                        "Matched source interface group%s %s", _except ? "-except" : "", _group)));
    return _except ? not(match) : match;
  }

  private void addIfMatching(Interface iface, ImmutableSet.Builder<String> matchingInterfaces) {
    Integer group =
        _family == Family.INET6 ? iface.getInterfaceGroup6() : iface.getInterfaceGroup();
    if (_group.includes(group)) {
      matchingInterfaces.add(iface.getName());
    }
  }

  private final boolean _except;
  private final @Nonnull Family _family;
  private final @Nonnull SubRange _group;
}
