package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.representation.juniper.FwTerm.Field;

public class FwFromInterfaceSet implements FwFrom {
  private final @Nonnull String _interfaceSetName;

  public FwFromInterfaceSet(String interfaceSetName) {
    _interfaceSetName = interfaceSetName;
  }

  @Override
  public Field getField() {
    return Field.SOURCE_INTERFACE;
  }

  public @Nonnull String getInterfaceSetName() {
    return _interfaceSetName;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    InterfaceSet interfaceSet =
        jc.getMasterLogicalSystem().getInterfaceSets().get(_interfaceSetName);
    if (interfaceSet == null) {
      w.redFlagf("Missing firewall interface-set '%s'", _interfaceSetName);
      return AclLineMatchExprs.FALSE;
    }

    // collect names of all configured physical and logical interfaces
    Set<String> configuredIfaces =
        new HashSet<>(jc.getMasterLogicalSystem().getInterfaces().keySet());
    jc.getMasterLogicalSystem().getInterfaces().values().stream()
        .flatMap(iface -> iface.getUnits().keySet().stream())
        .forEach(configuredIfaces::add);

    Set<String> interfaces =
        interfaceSet.getInterfaces().stream()
            .filter(configuredIfaces::contains)
            .collect(ImmutableSet.toImmutableSet());
    if (interfaces.isEmpty()) {
      w.redFlagf("Interface-set %s does not contain any valid interfaces", _interfaceSetName);
      return AclLineMatchExprs.FALSE;
    }
    return new MatchSrcInterface(
        interfaces, TraceElement.of(String.format("Matched interface-set %s", _interfaceSetName)));
  }
}
