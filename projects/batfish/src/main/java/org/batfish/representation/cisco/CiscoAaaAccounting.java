package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Vendor-specific model of Cisco IOS {@code aaa accounting} configuration */
@ParametersAreNonnullByDefault
public final class CiscoAaaAccounting implements Serializable {

  public static final String DEFAULT_COMMANDS = "default";

  private @Nonnull SortedMap<String, CiscoAaaAccountingCommands> _commands;
  private @Nullable CiscoAaaAccountingDefault _default;

  public CiscoAaaAccounting() {
    _commands = new TreeMap<>();
  }

  public @Nonnull SortedMap<String, CiscoAaaAccountingCommands> getCommands() {
    return _commands;
  }

  public void setCommands(SortedMap<String, CiscoAaaAccountingCommands> commands) {
    _commands = commands;
  }

  public @Nullable CiscoAaaAccountingDefault getDefault() {
    return _default;
  }

  public void setDefault(@Nullable CiscoAaaAccountingDefault accountingDefault) {
    _default = accountingDefault;
  }
}
