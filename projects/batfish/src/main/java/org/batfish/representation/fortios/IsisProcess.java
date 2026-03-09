package org.batfish.representation.fortios;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing IS-IS configuration */
public final class IsisProcess implements Serializable {
  public enum Level {
    LEVEL_1,
    LEVEL_2,
    LEVEL_1_2
  }

  public static final Level DEFAULT_IS_TYPE = Level.LEVEL_1_2;

  public IsisProcess() {
    _interfaces = new HashMap<>();
  }

  public @Nullable String getNetAddress() {
    return _netAddress;
  }

  public @Nonnull Level getIsTypeEffective() {
    return _isType != null ? _isType : DEFAULT_IS_TYPE;
  }

  public @Nullable Level getIsType() {
    return _isType;
  }

  public @Nonnull Map<String, IsisInterface> getInterfaces() {
    return _interfaces;
  }

  public void setNetAddress(String netAddress) {
    _netAddress = netAddress;
  }

  public void setIsType(Level isType) {
    _isType = isType;
  }

  private @Nullable String _netAddress;
  private @Nullable Level _isType;
  private final @Nonnull Map<String, IsisInterface> _interfaces;
}
