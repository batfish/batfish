package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents the content of ports.conf */
@ParametersAreNonnullByDefault
public final class CumulusPortsConfiguration implements Serializable {

  /** settings defined in the file */
  public static final class PortSettings implements Serializable {
    @Nullable private Integer _speed;
    @Nullable private Boolean _disabled;

    @Nullable
    public Integer getSpeed() {
      return _speed;
    }

    public void setSpeed(@Nullable Integer speed) {
      _speed = speed;
    }

    @Nullable
    public Boolean getDisabled() {
      return _disabled;
    }

    public void setDisabled(@Nullable Boolean disabled) {
      _disabled = disabled;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof PortSettings)) {
        return false;
      }
      PortSettings that = (PortSettings) o;
      return Objects.equals(_speed, that._speed) && Objects.equals(_disabled, that._disabled);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_speed, _disabled);
    }

    public static Builder builder() {
      return new Builder();
    }

    public static final class Builder {

      private Integer _speed;
      private Boolean _disabled;

      private Builder() {}

      public Builder setSpeed(Integer speed) {
        _speed = speed;
        return this;
      }

      public Builder setDisabled(Boolean disabled) {
        _disabled = disabled;
        return this;
      }

      public PortSettings build() {
        PortSettings settings = new PortSettings();
        settings.setSpeed(_speed);
        settings.setDisabled(_disabled);
        return settings;
      }
    }
  }

  @Nonnull private final Map<String, PortSettings> _portSettings;

  public CumulusPortsConfiguration() {
    _portSettings = new HashMap<>();
  }

  public void setSpeed(String ifaceName, int speedMbps) {
    _portSettings.computeIfAbsent(ifaceName, iface -> new PortSettings()).setSpeed(speedMbps);
  }

  public void setDisabled(String ifaceName, boolean disabled) {
    _portSettings.computeIfAbsent(ifaceName, iface -> new PortSettings()).setDisabled(disabled);
  }

  @Nonnull
  public Map<String, PortSettings> getPortSettings() {
    return _portSettings;
  }
}
