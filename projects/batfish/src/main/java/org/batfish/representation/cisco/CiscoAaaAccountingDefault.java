package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Vendor-specific model of Cisco IOS {@code aaa accounting ... default} configuration */
@ParametersAreNonnullByDefault
public final class CiscoAaaAccountingDefault implements Serializable {

  private @Nullable List<String> _groups;
  private @Nullable Boolean _local;

  /** The ordered list of AAA server groups this default accounting method targets */
  public @Nullable List<String> getGroups() {
    return _groups;
  }

  public void setGroups(@Nullable List<String> groups) {
    _groups = groups;
  }

  /** Whether {@code local} accounting is configured for the default method */
  public @Nullable Boolean getLocal() {
    return _local;
  }

  public void setLocal(@Nullable Boolean local) {
    _local = local;
  }
}
