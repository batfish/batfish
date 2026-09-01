package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AaaAuthenticationLoginList;

/**
 * Vendor-specific model of Cisco IOS {@code aaa authentication login} configuration.
 *
 * <p>Holds the named authentication method lists (the {@code default} list plus any custom lists)
 * and whether {@code aaa authentication login privilege-mode} is enabled. The method-list values
 * reuse the shared {@link AaaAuthenticationLoginList} datamodel type.
 */
@ParametersAreNonnullByDefault
public final class CiscoAaaAuthenticationLogin implements Serializable {

  public static final String DEFAULT_LIST_NAME = "default";

  private @Nonnull SortedMap<String, AaaAuthenticationLoginList> _lists;
  private boolean _privilegeMode;

  public CiscoAaaAuthenticationLogin() {
    _lists = new TreeMap<>();
  }

  public @Nonnull SortedMap<String, AaaAuthenticationLoginList> getLists() {
    return _lists;
  }

  public void setLists(SortedMap<String, AaaAuthenticationLoginList> lists) {
    _lists = lists;
  }

  public boolean getPrivilegeMode() {
    return _privilegeMode;
  }

  public void setPrivilegeMode(boolean privilegeMode) {
    _privilegeMode = privilegeMode;
  }
}
