package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/**
 * An SR-OS {@code router "Base" aggregates aggregate <prefix>} entry: a summary route activated by
 * a contributing more-specific route in the RIB. Models the subset that affects conversion: the
 * prefix, {@code summary-only} (suppress the contributing more-specifics on export), and any {@code
 * community} values attached to the aggregate.
 *
 * <p>Confirmed on SR-SIM 26.3.R1: the aggregate installs with protocol {@code aggregate},
 * preference 130, as a discard route, only when a contributing route exists.
 */
public final class Aggregate implements Serializable {

  /** SR-OS aggregate route preference (Batfish admin distance), device-confirmed. */
  public static final int PREFERENCE = 130;

  public Aggregate(Prefix prefix) {
    _prefix = prefix;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  /** Whether {@code summary-only} is set (advertise only the aggregate, suppress contributors). */
  public boolean getSummaryOnly() {
    return _summaryOnly;
  }

  public void setSummaryOnly(boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }

  /** The {@code community} values attached to the aggregate (e.g. {@code 65001:100}). */
  public @Nonnull List<String> getCommunities() {
    return _communities;
  }

  private final @Nonnull Prefix _prefix;
  private boolean _summaryOnly;
  private final @Nonnull List<String> _communities = new ArrayList<>();
}
