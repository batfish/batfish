package org.batfish.representation.f5_bigip;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a firewall rule in F5 BIG-IP security configuration. */
@ParametersAreNonnullByDefault
public final class FirewallRule implements Serializable {

  public static final class Builder {
    private @Nullable String _name;
    private @Nullable String _action;
    private @Nullable String _ipProtocol;

    private Builder() {}

    public @Nonnull Builder setName(@Nonnull String name) {
      _name = checkNotNull(name);
      return this;
    }

    public @Nonnull Builder setAction(@Nullable String action) {
      _action = action;
      return this;
    }

    public @Nonnull Builder setIpProtocol(@Nullable String ipProtocol) {
      _ipProtocol = ipProtocol;
      return this;
    }

    public @Nonnull FirewallRule build() {
      return new FirewallRule(checkNotNull(_name, "Name cannot be null"), _action, _ipProtocol);
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull String _name;
  private final @Nullable String _action;
  private final @Nullable String _ipProtocol;

  private FirewallRule(@Nonnull String name, @Nullable String action, @Nullable String ipProtocol) {
    _name = name;
    _action = action;
    _ipProtocol = ipProtocol;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getAction() {
    return _action;
  }

  public @Nullable String getIpProtocol() {
    return _ipProtocol;
  }

  public @Nonnull Builder toBuilder() {
    return builder().setName(_name).setAction(_action).setIpProtocol(_ipProtocol);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FirewallRule)) {
      return false;
    }
    FirewallRule that = (FirewallRule) o;
    return _name.equals(that._name)
        && java.util.Objects.equals(_action, that._action)
        && java.util.Objects.equals(_ipProtocol, that._ipProtocol);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(_name, _action, _ipProtocol);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("name: \"%s\"", _name));
    if (_action != null) {
      sb.append(",\naction: ").append(_action);
    }
    if (_ipProtocol != null) {
      sb.append(",\nip-protocol: ").append(_ipProtocol);
    }
    return sb.toString();
  }
}
