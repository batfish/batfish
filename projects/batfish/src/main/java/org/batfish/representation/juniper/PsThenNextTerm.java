package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * The {@code then next term} action. Causes Junos to skip the remaining actions in this term and
 * continue evaluating the next term in the same policy. When present in a {@code then} block, it
 * suppresses any bare {@code accept}/{@code reject} in the same block; suppression is handled in
 * {@link JuniperConfiguration#toStatements}, so this {@link #applyTo} emits no statement and the
 * term naturally falls through to the next.
 */
public final class PsThenNextTerm extends PsThen {

  public static final PsThenNextTerm INSTANCE = new PsThenNextTerm();

  private PsThenNextTerm() {}

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings w) {
    // No-op: term naturally falls through when no exit/return statement is emitted.
  }
}
