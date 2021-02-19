package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy (policy statement) which sets the tag for a
 * matched route.
 */
public final class PsThenTag extends PsThen {

  private final long _tag;

  public PsThenTag(long tag) {
    _tag = tag;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    statements.add(new SetTag(new LiteralLong(_tag)));
  }

  public long getTag() {
    return _tag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenTag)) {
      return false;
    }
    PsThenTag psThenTag = (PsThenTag) o;
    return _tag == psThenTag._tag;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_tag);
  }
}
