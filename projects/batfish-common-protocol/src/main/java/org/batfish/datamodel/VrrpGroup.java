package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
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
    private @Nonnull ImmutableSet.Builder<Ip> _virtualAddresses;

    public @Nonnull VrrpGroup build() {
      return new VrrpGroup(_preempt, _priority, _sourceAddress, _virtualAddresses.build());
    }

    public @Nonnull Builder setPriority(int priority) {
      _priority = priority;
      return this;
    }

    public @Nonnull Builder setPreempt(boolean preempt) {
      _preempt = preempt;
      return this;
    }

    public @Nonnull Builder setSourceAddress(@Nullable ConcreteInterfaceAddress sourceAddress) {
      _sourceAddress = sourceAddress;
      return this;
    }

    public @Nonnull Builder addVirtualAddress(Ip virtualAddress) {
      _virtualAddresses.add(virtualAddress);
      return this;
    }

    public @Nonnull Builder addVirtualAddresses(Iterable<Ip> virtualAddresses) {
      _virtualAddresses.addAll(virtualAddresses);
      return this;
    }

    public @Nonnull Builder setVirtualAddresses(Ip virtualAddress) {
      _virtualAddresses = ImmutableSet.<Ip>builder().add(virtualAddress);
      return this;
    }

    public @Nonnull Builder setVirtualAddresses(Iterable<Ip> virtualAddresses) {
      _virtualAddresses = ImmutableSet.<Ip>builder().addAll(virtualAddresses);
      return this;
    }

    private Builder() {
      _virtualAddresses = ImmutableSet.builder();
    }
  }

  private static final String PROP_PREEMPT = "preempt";
  private static final String PROP_PRIORITY = "priority";
  private static final String PROP_SOURCE_ADDRESS = "sourceAddress";
  private static final String PROP_VIRTUAL_ADDRESSES = "virtualAddresses";

  private boolean _preempt;
  private int _priority;
  private @Nullable ConcreteInterfaceAddress _sourceAddress;
  private @Nonnull Set<Ip> _virtualAddresses;

  public static Builder builder() {
    return new Builder();
  }

  public VrrpGroup(
      boolean preempt,
      int priority,
      @Nullable ConcreteInterfaceAddress sourceAddress,
      Set<Ip> virtualAddresses) {
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
      @JsonProperty(PROP_VIRTUAL_ADDRESSES) @Nullable Iterable<Ip> virtualAddresses) {
    return new VrrpGroup(
        firstNonNull(preempt, Boolean.FALSE),
        firstNonNull(priority, 0),
        sourceAddress,
        ImmutableSet.copyOf(firstNonNull(virtualAddresses, ImmutableSet.of())));
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

  @JsonIgnore
  public @Nonnull Set<Ip> getVirtualAddresses() {
    return _virtualAddresses;
  }

  @JsonProperty(PROP_VIRTUAL_ADDRESSES)
  private @Nonnull SortedSet<Ip> getVirtualAddressesSorted() {
    return ImmutableSortedSet.copyOf(_virtualAddresses);
  }
}
