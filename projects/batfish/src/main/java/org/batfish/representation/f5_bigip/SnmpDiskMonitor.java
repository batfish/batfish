package org.batfish.representation.f5_bigip;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an SNMP disk monitor configuration. */
@ParametersAreNonnullByDefault
public final class SnmpDiskMonitor {

  public static final class Builder {
    private @Nullable String _name;
    private @Nullable String _path;
    private @Nullable Integer _minSpace;

    private Builder() {}

    public @Nonnull Builder setName(@Nonnull String name) {
      _name = checkNotNull(name);
      return this;
    }

    public @Nonnull Builder setPath(@Nonnull String path) {
      _path = checkNotNull(path);
      return this;
    }

    public @Nonnull Builder setMinSpace(@Nullable Integer minSpace) {
      _minSpace = minSpace;
      return this;
    }

    public @Nonnull SnmpDiskMonitor build() {
      return new SnmpDiskMonitor(
          checkNotNull(_name, "Name cannot be null"),
          checkNotNull(_path, "Path cannot be null"),
          _minSpace);
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull String _name;
  private final @Nonnull String _path;
  private final @Nullable Integer _minSpace;

  private SnmpDiskMonitor(@Nonnull String name, @Nonnull String path, @Nullable Integer minSpace) {
    _name = name;
    _path = path;
    _minSpace = minSpace;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull String getPath() {
    return _path;
  }

  public @Nullable Integer getMinSpace() {
    return _minSpace;
  }

  public @Nonnull Builder toBuilder() {
    return builder().setName(_name).setPath(_path).setMinSpace(_minSpace);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SnmpDiskMonitor)) {
      return false;
    }
    SnmpDiskMonitor that = (SnmpDiskMonitor) o;
    return _name.equals(that._name)
        && _path.equals(that._path)
        && java.util.Objects.equals(_minSpace, that._minSpace);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(_name, _path, _minSpace);
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean withDaml) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("name: \"%s\"", _name));
    if (_path != null) {
      sb.append(",\npath: ").append(_path);
    }
    if (_minSpace != null) {
      sb.append(",\nminspace: ").append(_minSpace);
    }
    return sb.toString();
  }
}
