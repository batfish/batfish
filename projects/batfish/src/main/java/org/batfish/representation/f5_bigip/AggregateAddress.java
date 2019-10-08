package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** An aggregate prefix for which more specific prefixes should be aggregated in BGP. */
public final class AggregateAddress implements Serializable {

  public AggregateAddress(Prefix prefix) {
    _prefix = prefix;
  }

  public boolean getAsSet() {
    return _asSet;
  }

  public void setAsSet(boolean asSet) {
    _asSet = asSet;
  }

  public boolean getSummaryOnly() {
    return _summaryOnly;
  }

  public void setSummaryOnly(boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  private boolean _asSet;
  private final @Nonnull Prefix _prefix;
  private boolean _summaryOnly;
}
