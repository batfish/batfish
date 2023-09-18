package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.representation.juniper.FwTerm.Field;

public class FwFromInterface implements FwFrom {
  private final @Nonnull String _interfaceName;

  public FwFromInterface(String interfaceSetName) {
    _interfaceName = interfaceSetName;
  }

  @Override
  public Field getField() {
    return Field.SOURCE_INTERFACE;
  }

  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    Map<String, Interface> configuredIfaces = jc.getMasterLogicalSystem().getInterfaces();
    if (!configuredIfaces.containsKey(_interfaceName)
        && configuredIfaces.values().stream()
            .flatMap(iface -> iface.getUnits().keySet().stream())
            .noneMatch(_interfaceName::equals)) {
      w.redFlagf("Missing interface '%s'", _interfaceName);
      return AclLineMatchExprs.FALSE;
    }
    return new MatchSrcInterface(
        ImmutableList.of(_interfaceName),
        String.format("Matched source interface %s", _interfaceName));
  }
}
