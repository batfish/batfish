package org.batfish.representation.juniper;

import java.util.List;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.SetTunnelAttribute;
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
    // If the specified tunnel attribute is undefined, it will show up as an undefined reference
    if (c.getTunnelAttributes().containsKey(_tunnelAttrName)) {
      statements.add(new SetTunnelAttribute(_tunnelAttrName));
    }
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
