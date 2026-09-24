package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;

import com.google.common.collect.ImmutableSet;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.common.util.PatternProvider;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.representation.juniper.FwTerm.Field;

/** Matches traffic received on an interface selected by a Junos interface wildcard. */
@ParametersAreNonnullByDefault
public final class FwFromInterfaceWildcard implements FwFrom {

  public FwFromInterfaceWildcard(String interfaceWildcard) {
    _interfaceWildcard = interfaceWildcard;
  }

  @Override
  public Field getField() {
    return Field.SOURCE_INTERFACE;
  }

  public @Nonnull String getInterfaceWildcard() {
    return _interfaceWildcard;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    Pattern pattern = PatternProvider.fromString(GroupWildcard.toJavaRegex(_interfaceWildcard));
    ImmutableSet.Builder<String> matchingInterfaces = ImmutableSet.builder();
    for (Interface iface : jc.getMasterLogicalSystem().getInterfaces().values()) {
      addIfMatching(iface, pattern, matchingInterfaces);
      for (Interface unit : iface.getUnits().values()) {
        addIfMatching(unit, pattern, matchingInterfaces);
      }
    }
    ImmutableSet<String> interfaces = matchingInterfaces.build();
    if (interfaces.isEmpty()) {
      w.redFlagf(
          "Interface wildcard '%s' does not match any configured interfaces", _interfaceWildcard);
      return FALSE;
    }
    return new MatchSrcInterface(
        interfaces,
        TraceElement.of(String.format("Matched source interface wildcard %s", _interfaceWildcard)));
  }

  private static void addIfMatching(
      Interface iface, Pattern pattern, ImmutableSet.Builder<String> matchingInterfaces) {
    if (pattern.matcher(iface.getName()).matches()) {
      matchingInterfaces.add(iface.getName());
    }
  }

  private final @Nonnull String _interfaceWildcard;
}
