package org.batfish.representation.terraform;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Common properties for all Terraform resources */
@ParametersAreNonnullByDefault
final class CommonResourceProperties implements Serializable {

  public enum Mode {
    DATA,
    MANAGED;

    @JsonCreator
    public static Mode fromString(String mode) {
      return Mode.valueOf(mode.toUpperCase());
    }
  }

  @Nonnull private final Mode _mode;
  @Nonnull private final String _type;
  @Nonnull private final String _name;
  @Nonnull private final String _provider;

  CommonResourceProperties(Mode mode, String type, String name, String provider) {
    _mode = mode;
    _type = type;
    _name = name;
    _provider = provider;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CommonResourceProperties)) {
      return false;
    }
    CommonResourceProperties that = (CommonResourceProperties) o;
    return _mode == that._mode
        && _type.equals(that._type)
        && _name.equals(that._name)
        && _provider.equals(that._provider);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_mode, _type, _name, _provider);
  }

  @Nonnull
  public String getProvider() {
    return _provider;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public String getType() {
    return _type;
  }

  @Nonnull
  public Mode getMode() {
    return _mode;
  }
}
