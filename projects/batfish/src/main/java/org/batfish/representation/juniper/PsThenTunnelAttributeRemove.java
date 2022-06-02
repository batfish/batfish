package org.batfish.representation.juniper;

import java.util.List;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.RemoveTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@link Statement} that clears tunnel attributes on the route. */
public final class PsThenTunnelAttributeRemove extends PsThen {

  public static final PsThenTunnelAttributeRemove INSTANCE = new PsThenTunnelAttributeRemove();

  private PsThenTunnelAttributeRemove() {}

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings w) {
    statements.add(RemoveTunnelEncapsulationAttribute.instance());
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof PsThenTunnelAttributeRemove;
  }

  @Override
  public int hashCode() {
    return getClass().getCanonicalName().hashCode();
  }
}
