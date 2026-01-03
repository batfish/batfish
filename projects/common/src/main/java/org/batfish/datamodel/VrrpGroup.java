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
import org.batfish.datamodel.tracking.TrackAction;

/**
 * Configuration for a VRID on a router.
 *
 * <p>Hybrid model supporting RFC VRRP, VRRP-A (A10), and CheckPoint cluster IPs.
 *
 * <ul>
 *   <li>For RFC VRRP, all IPs should be assigned to a single interface, and that interface should
 *       be the same as the sync inteface on which the {@link VrrpGroup} sits.
 *   <li>TODO: document virtual addresses for VRRP-A (A10)
 *   <li>For CheckPoint cluster IPs, each entry should be a physical or logical interface interface
 *       on the device other than the Sync interface, and this VrrpGroup should sit on the Sync
 *       interface.
 * </ul>
 *
 * TODO: Current limitation: for CheckPoint, if an interface on which to assign IPs is down and this
 * virtual router is master, the IPs to be assigned to that interface will not be assigned to any
 * other cluster member.
 */
@ParametersAreNonnullByDefault
public final class VrrpGroup implements Serializable {

  public static final int MAX_PRIORITY = 255;

  public static final class Builder {
    private boolean _preempt;
    private int _priority;
    private @Nullable ConcreteInterfaceAddress _sourceAddress;
    private @Nonnull Map<String, TrackAction> _trackActions;
    private @Nonnull Map<String, ImmutableSet.Builder<Ip>> _virtualAddresses;

    public @Nonnull VrrpGroup build() {
      return new VrrpGroup(
          _preempt, _priority, _sourceAddress, _trackActions, buildVirtualAddresses());
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

    public @Nonnull Builder setVirtualAddresses(Map<String, Set<Ip>> virtualAddresses) {
      _virtualAddresses = new HashMap<>();
      virtualAddresses.forEach(this::addVirtualAddresses);
      return this;
    }

    public @Nonnull Builder setTrackActions(Map<String, TrackAction> trackActions) {
      _trackActions = trackActions;
      return this;
    }

    private Builder() {
      _virtualAddresses = new HashMap<>();
      _trackActions = ImmutableMap.of();
    }
  }

  private static final String PROP_PREEMPT = "preempt";
  private static final String PROP_PRIORITY = "priority";
  private static final String PROP_SOURCE_ADDRESS = "sourceAddress";
  private static final String PROP_TRACK_ACTIONS = "trackActions";
  private static final String PROP_VIRTUAL_ADDRESSES = "virtualAddresses";

  private final boolean _preempt;
  private final int _priority;
  private final @Nullable ConcreteInterfaceAddress _sourceAddress;
  private final @Nonnull Map<String, TrackAction> _trackActions;
  private final @Nonnull Map<String, Set<Ip>> _virtualAddresses;

  public static Builder builder() {
    return new Builder();
  }

  public VrrpGroup(
      boolean preempt,
      int priority,
      @Nullable ConcreteInterfaceAddress sourceAddress,
      Map<String, TrackAction> trackActions,
      Map<String, Set<Ip>> virtualAddresses) {
    _preempt = preempt;
    _priority = priority;
    _sourceAddress = sourceAddress;
    _trackActions = trackActions;
    _virtualAddresses = virtualAddresses;
  }

  @JsonCreator
  private static @Nonnull VrrpGroup create(
      @JsonProperty(PROP_PREEMPT) @Nullable Boolean preempt,
      @JsonProperty(PROP_PRIORITY) @Nullable Integer priority,
      @JsonProperty(PROP_SOURCE_ADDRESS) @Nullable ConcreteInterfaceAddress sourceAddress,
      @JsonProperty(PROP_TRACK_ACTIONS) @Nullable Map<String, TrackAction> trackActions,
      @JsonProperty(PROP_VIRTUAL_ADDRESSES) @Nullable Map<String, Set<Ip>> virtualAddresses) {
    return new VrrpGroup(
        firstNonNull(preempt, Boolean.FALSE),
        firstNonNull(priority, 0),
        sourceAddress,
        ImmutableMap.copyOf(firstNonNull(trackActions, ImmutableMap.of())),
        ImmutableMap.copyOf(firstNonNull(virtualAddresses, ImmutableMap.of())));
  }

  public @Nonnull Builder toBuilder() {
    Builder builder =
        VrrpGroup.builder()
            .setPreempt(_preempt)
            .setPriority(_priority)
            .setSourceAddress(_sourceAddress)
            .setTrackActions(_trackActions)
            .setVirtualAddresses(_virtualAddresses);
    return builder;
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
        && _trackActions.equals(that._trackActions)
        && _virtualAddresses.equals(that._virtualAddresses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_preempt, _priority, _sourceAddress, _trackActions, _virtualAddresses);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .add(PROP_PREEMPT, _preempt)
        .add(PROP_PRIORITY, _priority)
        .add(PROP_SOURCE_ADDRESS, _sourceAddress)
        .add(PROP_TRACK_ACTIONS, _trackActions)
        .add(PROP_VIRTUAL_ADDRESSES, _virtualAddresses)
        .toString();
  }

  /**
   * Whether this virtual router should overtake a lower-priority existing master. This property is
   * informational only, since Batfish does not model VRRP activation order.
   */
  @JsonProperty(PROP_PREEMPT)
  public boolean getPreempt() {
    return _preempt;
  }

  /** Priority during an election for this VRID. Higher priority is more preferred. */
  @JsonProperty(PROP_PRIORITY)
  public int getPriority() {
    return _priority;
  }

  /** The address from which VRRP control traffic is sent. */
  @JsonProperty(PROP_SOURCE_ADDRESS)
  public @Nullable ConcreteInterfaceAddress getSourceAddress() {
    return _sourceAddress;
  }

  /** Map from track method name to action to take if track method evalutes to {@code true}. */
  @JsonIgnore
  public @Nonnull Map<String, TrackAction> getTrackActions() {
    return _trackActions;
  }

  @JsonProperty(PROP_TRACK_ACTIONS)
  private @Nonnull Map<String, TrackAction> getTrackActionsSorted() {
    return ImmutableSortedMap.copyOf(_trackActions);
  }

  /**
   * Virtual addresses assigned to interfaces if this virtual router is the winner of the election
   * for this VRID.
   *
   * <p>Map: interface on which to assign one or more IPs -> IPs to assign to that interface.
   */
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
