package org.batfish.representation.f5_bigip;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an SNMP process monitor configuration. */
@ParametersAreNonnullByDefault
public final class SnmpProcessMonitor {

  public static final class Builder {
    private @Nullable String _name;
    private @Nullable String _process;
    private @Nullable Integer _maxProcesses;

    private Builder() {}

    public @Nonnull Builder setName(@Nonnull String name) {
      _name = checkNotNull(name);
      return this;
    }

    public @Nonnull Builder setProcess(@Nonnull String process) {
      _process = checkNotNull(process);
      return this;
    }

    public @Nonnull Builder setMaxProcesses(@Nullable Integer maxProcesses) {
      _maxProcesses = maxProcesses;
      return this;
    }

    public @Nonnull SnmpProcessMonitor build() {
      return new SnmpProcessMonitor(
          checkNotNull(_name, "Name cannot be null"),
          checkNotNull(_process, "Process cannot be null"),
          _maxProcesses);
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull String _name;
  private final @Nonnull String _process;
  private final @Nullable Integer _maxProcesses;

  private SnmpProcessMonitor(
      @Nonnull String name, @Nonnull String process, @Nullable Integer maxProcesses) {
    _name = name;
    _process = process;
    _maxProcesses = maxProcesses;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull String getProcess() {
    return _process;
  }

  public @Nullable Integer getMaxProcesses() {
    return _maxProcesses;
  }

  public @Nonnull Builder toBuilder() {
    return builder().setName(_name).setProcess(_process).setMaxProcesses(_maxProcesses);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SnmpProcessMonitor)) {
      return false;
    }
    SnmpProcessMonitor that = (SnmpProcessMonitor) o;
    return _name.equals(that._name)
        && _process.equals(that._process)
        && java.util.Objects.equals(_maxProcesses, that._maxProcesses);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(_name, _process, _maxProcesses);
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean withDaml) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("name: \"%s\"", _name));
    if (_process != null) {
      sb.append(",\nprocess: ").append(_process);
    }
    if (_maxProcesses != null) {
      sb.append(",\nmax-processes: ").append(_maxProcesses);
    }
    return sb.toString();
  }
}
