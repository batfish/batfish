package org.batfish.representation.juniper;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy (policy statement) which sets the source-class
 * for a matched route. Source-class is used for accounting/metering traffic in firewall filters.
 */
public final class PsThenSourceClass extends PsThen {

  private final @Nonnull String _sourceClassName;

  public PsThenSourceClass(String sourceClassName) {
    _sourceClassName = sourceClassName;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // Source-class assignment is used for accounting/metering in Junos.
    // Batfish does not currently model this functionality.
  }

  public @Nonnull String getSourceClassName() {
    return _sourceClassName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenSourceClass)) {
      return false;
    }
    PsThenSourceClass that = (PsThenSourceClass) o;
    return _sourceClassName.equals(that._sourceClassName);
  }

  @Override
  public int hashCode() {
    return _sourceClassName.hashCode();
  }
}
