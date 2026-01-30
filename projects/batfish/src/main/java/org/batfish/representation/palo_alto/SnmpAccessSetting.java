package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents SNMP access settings for a Palo Alto device */
@ParametersAreNonnullByDefault
public final class SnmpAccessSetting implements Serializable {

  private final @Nonnull String _version;
  private final @Nonnull List<String> _communityStrings;

  public SnmpAccessSetting(String version) {
    _version = version;
    _communityStrings = new ArrayList<>();
  }

  public void addCommunityString(String communityString) {
    _communityStrings.add(communityString);
  }

  public @Nonnull String getVersion() {
    return _version;
  }

  public @Nonnull List<String> getCommunityStrings() {
    return _communityStrings;
  }
}
