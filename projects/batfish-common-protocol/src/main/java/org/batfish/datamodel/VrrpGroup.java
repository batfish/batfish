package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration for a VRID on a router. */
@ParametersAreNonnullByDefault
public final class VrrpGroup implements Serializable {

  public static final int MAX_PRIORITY = 255;

  public static final class Builder {
    private boolean _preempt;
    private int _priority;
    private @Nullable ConcreteInterfaceAddress _sourceAddress;
    private @Nonnull Map<String, ImmutableSet.Builder<Ip>> _virtualAddresses;

    public @Nonnull VrrpGroup build() {
      return new VrrpGroup(_preempt, _priority, _sourceAddress, buildVirtualAddresses());
    }

    private @Nonnull Map<String, Set<Ip>> buildVirtualAddresses() {
      ImmutableMap.Builder<String, Set<Ip>> virtualAddressesBuilder = ImmutableMap.builder();
      _virtualAddresses.forEach(
          (iface, ipsBuilder) -> virtualAddressesBuilder.put(iface, ipsBuilder.build()));
      return virtualAddressesBuilder.build();
    }

    public @Nonnull Builder setPriority(int priority) {
      _priority = priority;
      return this;
    }

    public @Nonnull Builder setPreempt(boolean preempt) {
      _preempt = preempt;
      return this;
    }

    public @Nonnull Map<String, Set<Ip>> getVirtualAddresses() {
      return buildVirtualAddresses();
    }

    public @Nonnull Builder setSourceAddress(@Nullable ConcreteInterfaceAddress sourceAddress) {
      _sourceAddress = sourceAddress;
      return this;
    }

    public @Nonnull Builder addVirtualAddress(String receivingInterface, Ip virtualAddress) {
      _virtualAddresses
          .computeIfAbsent(receivingInterface, iface -> ImmutableSet.builder())
          .add(virtualAddress);
      return this;
    }

    public @Nonnull Builder addVirtualAddresses(
        String receivingInterface, Iterable<Ip> virtualAddresses) {
      _virtualAddresses
          .computeIfAbsent(receivingInterface, iface -> ImmutableSet.builder())
          .addAll(virtualAddresses);
      return this;
    }

    public @Nonnull Builder setVirtualAddresses(String receivingInterface, Ip virtualAddress) {
      _virtualAddresses = new HashMap<>();
      addVirtualAddress(receivingInterface, virtualAddress);
      return this;
    }

    public @Nonnull Builder setVirtualAddresses(
        String receivingInterface, Iterable<Ip> virtualAddresses) {
      _virtualAddresses = new HashMap<>();
      addVirtualAddresses(receivingInterface, virtualAddresses);
      return this;
    }

    private Builder() {
      _virtualAddresses = new HashMap<>();
    }
  }

  private static final String PROP_PREEMPT = "preempt";
  private static final String PROP_PRIORITY = "priority";
  private static final String PROP_SOURCE_ADDRESS = "sourceAddress";
  private static final String PROP_VIRTUAL_ADDRESSES = "virtualAddresses";

  private boolean _preempt;
  private int _priority;
  private @Nullable ConcreteInterfaceAddress _sourceAddress;
  private @Nonnull Map<String, Set<Ip>> _virtualAddresses;

  public static Builder builder() {
    return new Builder();
  }

  public VrrpGroup(
      boolean preempt,
      int priority,
      @Nullable ConcreteInterfaceAddress sourceAddress,
      Map<String, Set<Ip>> virtualAddresses) {
    _preempt = preempt;
    _priority = priority;
    _sourceAddress = sourceAddress;
    _virtualAddresses = virtualAddresses;
  }

  @JsonCreator
  private static @Nonnull VrrpGroup create(
      @JsonProperty(PROP_PREEMPT) @Nullable Boolean preempt,
      @JsonProperty(PROP_PRIORITY) @Nullable Integer priority,
      @JsonProperty(PROP_SOURCE_ADDRESS) @Nullable ConcreteInterfaceAddress sourceAddress,
      @JsonProperty(PROP_VIRTUAL_ADDRESSES) @Nullable Map<String, Set<Ip>> virtualAddresses) {
    return new VrrpGroup(
        firstNonNull(preempt, Boolean.FALSE),
        firstNonNull(priority, 0),
        sourceAddress,
        ImmutableMap.copyOf(firstNonNull(virtualAddresses, ImmutableMap.of())));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof VrrpGroup)) {
      return false;
    }
    VrrpGroup that = (VrrpGroup) o;
    return _preempt == that._preempt
        && _priority == that._priority
        && Objects.equals(_sourceAddress, that._sourceAddress)
        && _virtualAddresses.equals(that._virtualAddresses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_preempt, _priority, _sourceAddress, _virtualAddresses);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add(PROP_PREEMPT, _preempt)
        .add(PROP_PRIORITY, _priority)
        .add(PROP_SOURCE_ADDRESS, _sourceAddress)
        .add(PROP_VIRTUAL_ADDRESSES, _virtualAddresses)
        .toString();
  }

  @JsonProperty(PROP_PREEMPT)
  public boolean getPreempt() {
    return _preempt;
  }

  @JsonProperty(PROP_PRIORITY)
  public int getPriority() {
    return _priority;
  }

  @JsonProperty(PROP_SOURCE_ADDRESS)
  public @Nullable ConcreteInterfaceAddress getSourceAddress() {
    return _sourceAddress;
  }

  /** interface receiving IPs -> IPs */
  @JsonIgnore
  public @Nonnull Map<String, Set<Ip>> getVirtualAddresses() {
    return _virtualAddresses;
  }

  @JsonProperty(PROP_VIRTUAL_ADDRESSES)
  private @Nonnull SortedMap<String, SortedSet<Ip>> getVirtualAddressesSorted() {
    ImmutableSortedMap.Builder<String, SortedSet<Ip>> builder = ImmutableSortedMap.naturalOrder();
    _virtualAddresses.forEach((iface, ips) -> builder.put(iface, ImmutableSortedSet.copyOf(ips)));
    return builder.build();
  }
}
