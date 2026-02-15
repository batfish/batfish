package org.batfish.vendor.huawei.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Interface configuration for Huawei device. */
public class HuaweiInterface {

  private @Nullable ConcreteInterfaceAddress _address;
  private @Nullable String _description;
  private final @Nonnull String _name;
  private boolean _shutdown;

  public HuaweiInterface(@Nonnull String name) {
    _name = name;
    _shutdown = false;
  }

  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  public void setAddress(@Nullable ConcreteInterfaceAddress address) {
    _address = address;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }
}
