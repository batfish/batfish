package org.batfish.engine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Everything the engine needs to build one model of a tenant's network: each device's config text,
 * the physical links between them, and the observed environment Batfish cannot compute.
 */
public record SnapshotRequest(
    @Nonnull List<Device> devices, @Nonnull List<Link> layer1, @Nonnull Environment environment) {

  private static final Pattern DEVICE_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$");

  /** Optional parts may be omitted, as in the HTTP contract. */
  public SnapshotRequest {
    devices = ImmutableList.copyOf(devices);
    layer1 = layer1 == null ? ImmutableList.of() : ImmutableList.copyOf(layer1);
    environment = environment == null ? Environment.NONE : environment;
  }

  /**
   * One device. {@code name} is a tenant-unique safe filename component. The engine uses its
   * lowercase form as the config hostname. {@code linuxInterfaces} is only used by {@link
   * Dialect#FRR_LINUX}, whose addresses live in the kernel rather than the FRR config.
   */
  public record Device(
      @Nonnull String name,
      @Nonnull Dialect dialect,
      @Nonnull String configText,
      @Nullable List<LinuxInterface> linuxInterfaces) {
    public Device {
      if (name == null || !DEVICE_NAME.matcher(name).matches()) {
        throw new IllegalArgumentException(
            "device name must be 1-128 safe filename characters, starting with a letter or digit");
      }
      if (dialect == null || configText == null) {
        throw new IllegalArgumentException("device dialect and configText are required");
      }
      linuxInterfaces = linuxInterfaces == null ? null : ImmutableList.copyOf(linuxInterfaces);
    }

    public Device(String name, Dialect dialect, String configText) {
      this(name, dialect, configText, null);
    }
  }

  /** A kernel interface on an FRR host: its name, CIDR addresses, and whether it is up. */
  public record LinuxInterface(@Nonnull String name, @Nonnull List<String> addresses, boolean up) {
    public LinuxInterface {
      addresses = addresses == null ? ImmutableList.of() : ImmutableList.copyOf(addresses);
    }
  }

  /** A physical link between two interfaces, by lowered node name and interface name. */
  public record Link(
      @Nonnull String node1,
      @Nonnull String interface1,
      @Nonnull String node2,
      @Nonnull String interface2) {}

  /** An interface by lowered node name and interface name. */
  public record NodeInterface(@Nonnull String node, @Nonnull String iface) {}

  /** Observed state Batfish cannot compute: interfaces whose line is down, and nodes down. */
  public record Environment(
      @Nonnull Set<NodeInterface> interfacesDown, @Nonnull Set<String> nodesDown) {
    public static final Environment NONE = new Environment(ImmutableSet.of(), ImmutableSet.of());

    public Environment {
      interfacesDown =
          interfacesDown == null ? ImmutableSet.of() : ImmutableSet.copyOf(interfacesDown);
      nodesDown = nodesDown == null ? ImmutableSet.of() : ImmutableSet.copyOf(nodesDown);
    }
  }
}
