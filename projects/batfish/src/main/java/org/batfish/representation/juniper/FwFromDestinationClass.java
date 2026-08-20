package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from destination-class */
public final class FwFromDestinationClass implements FwFrom {

  private final @Nonnull String _name;

  public FwFromDestinationClass(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public Field getField() {
    return Field.DESTINATION;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    // Destination-class matching is used for accounting/metering in Junos based on routes
    // classified by policy-statement "then destination-class" actions.
    // Batfish does not currently model this functionality.
    return new FalseExpr(getTraceElement());
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched destination-class %s", _name));
  }
}
