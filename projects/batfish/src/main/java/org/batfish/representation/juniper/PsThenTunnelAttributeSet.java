package org.batfish.representation.juniper;

import java.util.List;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.expr.LiteralTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.SetTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy (policy statement) which sets the tunnel
 * attribute for a matched route.
 */
public final class PsThenTunnelAttributeSet extends PsThen {

  private final String _tunnelAttrName;

  public PsThenTunnelAttributeSet(String tunnelAttrName) {
    _tunnelAttrName = tunnelAttrName;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    TunnelAttribute tunnelAttribute =
        juniperVendorConfiguration
            .getMasterLogicalSystem()
            .getTunnelAttributes()
            .get(_tunnelAttrName);
    if (tunnelAttribute == null) {
      // The missing tunnel attribute will show up as an undefined reference
      return;
    }
    TunnelAttribute.Type type = tunnelAttribute.getType();
    Ip remoteEndpoint = tunnelAttribute.getRemoteEndPoint();
    if (type == null || remoteEndpoint == null) {
      warnings.redFlagf(
          "Ignoring tunnel-attribute %s because its %s is not set",
          _tunnelAttrName, type == null ? "tunnel-type" : "remote-end-point");
      return;
    }
    statements.add(
        new SetTunnelEncapsulationAttribute(
            new LiteralTunnelEncapsulationAttribute(
                new TunnelEncapsulationAttribute(remoteEndpoint))));
  }

  public String getTunnelAttributeName() {
    return _tunnelAttrName;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenTunnelAttributeSet)) {
      return false;
    }
    PsThenTunnelAttributeSet psThenTunnelAttributeSet = (PsThenTunnelAttributeSet) o;
    return _tunnelAttrName.equals(psThenTunnelAttributeSet._tunnelAttrName);
  }

  @Override
  public int hashCode() {
    return _tunnelAttrName.hashCode();
  }
}
