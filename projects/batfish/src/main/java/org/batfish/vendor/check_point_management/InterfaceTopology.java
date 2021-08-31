package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** An interface topology as shown in show-gateways-and-servers */
public class InterfaceTopology implements Serializable {
  @JsonCreator
  private static @Nonnull InterfaceTopology create(
      @JsonProperty(PROP_LEADS_TO_INTERNET) @Nullable Boolean leadsToInternet) {
    checkArgument(leadsToInternet != null, "Missing %s", PROP_LEADS_TO_INTERNET);
    return new InterfaceTopology(leadsToInternet);
  }

  @VisibleForTesting
  InterfaceTopology(boolean leadsToInternet) {
    _leadsToInternet = leadsToInternet;
  }

  public boolean getLeadsToInternet() {
    return _leadsToInternet;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof InterfaceTopology)) {
      return false;
    }
    InterfaceTopology that = (InterfaceTopology) o;
    return _leadsToInternet == that._leadsToInternet;
  }

  @Override
  public int hashCode() {
    return Boolean.hashCode(_leadsToInternet);
  }

  private static final String PROP_LEADS_TO_INTERNET = "leads-to-internet";
  private final boolean _leadsToInternet;
}
