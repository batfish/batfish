package org.batfish.representation.palo_alto;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Palo Alto application object */
@ParametersAreNonnullByDefault
public final class Application implements Serializable {

  public static final class Builder {
    private final String _name;
    private @Nullable String _description;
    private @Nonnull Set<Service> _services;

    private Builder(@Nonnull String name) {
      _name = name;
      _services = new HashSet<>();
    }

    public @Nonnull Application build() {
      return new Application(_name, _description, _services);
    }

    public @Nonnull Builder setDescription(@Nullable String description) {
      _description = description;
      return this;
    }

    public @Nonnull Builder addService(@Nonnull Service service) {
      _services.add(service);
      return this;
    }
  }

  private @Nullable String _description;
  private final @Nonnull String _name;
  private @Nonnull Set<Service> _services;

  private Application(
      @Nonnull String name, @Nullable String description, @Nonnull Set<Service> services) {
    _name = name;
    _description = description;
    _services = ImmutableSet.copyOf(services);
  }

  public static @Nonnull Builder builder(String name) {
    return new Builder(name);
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Set<Service> getServices() {
    return _services;
  }

  public void setServices(@Nonnull Iterable<Service> services) {
    _services = ImmutableSet.copyOf(services);
  }
}
