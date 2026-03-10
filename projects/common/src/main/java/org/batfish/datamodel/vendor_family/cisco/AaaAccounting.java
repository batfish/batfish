package org.batfish.datamodel.vendor_family.cisco;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class AaaAccounting implements Serializable {

  public static final String DEFAULT_COMMANDS = "default";

  private SortedMap<String, AaaAccountingCommands> _commands;

  private AaaAccountingDefault _default;

  public AaaAccounting() {
    _commands = new TreeMap<>();
  }

  public SortedMap<String, AaaAccountingCommands> getCommands() {
    return _commands;
  }

  public AaaAccountingDefault getDefault() {
    return _default;
  }

  public void setCommands(SortedMap<String, AaaAccountingCommands> commands) {
    _commands = commands;
  }

  public void setDefault(AaaAccountingDefault default1) {
    _default = default1;
  }
}
