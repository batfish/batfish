package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Vendor-specific model of Cisco IOS {@code aaa} configuration */
@ParametersAreNonnullByDefault
public final class CiscoAaa implements Serializable {

  private boolean _newModel;
  private @Nullable CiscoAaaAuthentication _authentication;
  private @Nullable CiscoAaaAccounting _accounting;

  /** Whether {@code aaa new-model} is configured. */
  public boolean getNewModel() {
    return _newModel;
  }

  public void setNewModel(boolean newModel) {
    _newModel = newModel;
  }

  public @Nullable CiscoAaaAuthentication getAuthentication() {
    return _authentication;
  }

  public void setAuthentication(@Nullable CiscoAaaAuthentication authentication) {
    _authentication = authentication;
  }

  public @Nullable CiscoAaaAccounting getAccounting() {
    return _accounting;
  }

  public void setAccounting(@Nullable CiscoAaaAccounting accounting) {
    _accounting = accounting;
  }
}
