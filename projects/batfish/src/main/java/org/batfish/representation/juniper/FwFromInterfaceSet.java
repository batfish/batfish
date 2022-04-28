package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
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
    // interface sets for non-default logical system are not currently parsed
    InterfaceSet interfaceSet =
        jc.getMasterLogicalSystem().getInterfaceSets().get(_interfaceSetName);
    if (interfaceSet == null) {
      w.redFlag(String.format("Missing firewall interface-set '%s'", _interfaceSetName));
      return AclLineMatchExprs.FALSE;
    }

    Set<String> interfaces =
        interfaceSet.getInterfaces().stream()
            .filter(
                iface -> {
                  if (!jc.getMasterLogicalSystem().getInterfaces().containsKey(iface)) {
                    w.redFlag(
                        String.format(
                            "Interface-set %s references undefined interface %s. This interface"
                                + " will be ignored",
                            _interfaceSetName, iface));
                    return false;
                  }
                  return true;
                })
            .collect(ImmutableSet.toImmutableSet());
    if (interfaces.isEmpty()) {
      w.redFlag(
          String.format(
              "Interface-set %s does not contain any valid interfaces", _interfaceSetName));
      return AclLineMatchExprs.FALSE;
    }
    return new MatchSrcInterface(
        interfaces, TraceElement.of(String.format("Matched interface-set %s", _interfaceSetName)));
  }
}
