package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from source-class */
public final class FwFromSourceClass implements FwFrom {

  private final @Nonnull String _name;

  public FwFromSourceClass(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public Field getField() {
    return Field.SOURCE;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    // Source-class matching is used for accounting/metering in Junos based on routes
    // classified by policy-statement "then source-class" actions.
    // Batfish does not currently model this functionality.
    w.redFlag(
        String.format("Firewall filter match condition 'source-class %s' is not supported", _name));
    return new FalseExpr(getTraceElement());
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched source-class %s", _name));
  }
}
