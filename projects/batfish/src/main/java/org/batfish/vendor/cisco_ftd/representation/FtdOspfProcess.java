package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

public class FtdOspfProcess implements Serializable {

  private final @Nonnull String _name; // Process ID
  private final @Nonnull List<FtdOspfNetwork> _networks;
  private final @Nonnull Set<String> _passiveInterfaces;
  private @Nullable Ip _routerId;
  private boolean _passiveInterfaceDefault;

  public FtdOspfProcess(String name) {
    _name = name;
    _networks = new ArrayList<>();
    _passiveInterfaces = new HashSet<>();
  }

  public String getName() {
    return _name;
  }

  public List<FtdOspfNetwork> getNetworks() {
    return _networks;
  }

  public Set<String> getPassiveInterfaces() {
    return _passiveInterfaces;
  }

  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public boolean getPassiveInterfaceDefault() {
    return _passiveInterfaceDefault;
  }

  public void setPassiveInterfaceDefault(boolean passiveInterfaceDefault) {
    _passiveInterfaceDefault = passiveInterfaceDefault;
  }
}
