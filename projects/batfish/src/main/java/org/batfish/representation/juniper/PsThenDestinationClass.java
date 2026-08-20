package org.batfish.representation.juniper;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy (policy statement) which sets the
 * destination-class for a matched route. Destination-class is used for accounting/metering traffic
 * in firewall filters.
 */
public final class PsThenDestinationClass extends PsThen {

  private final @Nonnull String _destinationClassName;

  public PsThenDestinationClass(String destinationClassName) {
    _destinationClassName = destinationClassName;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // Destination-class assignment is used for accounting/metering in Junos.
    // Batfish does not currently model this functionality.
  }

  public @Nonnull String getDestinationClassName() {
    return _destinationClassName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenDestinationClass)) {
      return false;
    }
    PsThenDestinationClass that = (PsThenDestinationClass) o;
    return _destinationClassName.equals(that._destinationClassName);
  }

  @Override
  public int hashCode() {
    return _destinationClassName.hashCode();
  }
}
