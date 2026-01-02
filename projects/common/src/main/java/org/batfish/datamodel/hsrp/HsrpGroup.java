package org.batfish.datamodel.hsrp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.tracking.TrackAction;

/** An HSRP group defined on an {@link Interface} */
public final class HsrpGroup implements Serializable {

  public static final class Builder {
    private @Nullable String _authentication;
    private int _helloTime;
    private int _holdTime;
    private boolean _preempt;
    private int _priority;
    private @Nonnull SortedMap<String, TrackAction> _trackActions;
    private @Nullable ConcreteInterfaceAddress _sourceAddress;
    private @Nonnull Set<Ip> _virtualAddresses;

    private Builder() {
      _helloTime = DEFAULT_HELLO_TIME;
      _holdTime = DEFAULT_HOLD_TIME;
      _priority = DEFAULT_PRIORITY;
      _trackActions = ImmutableSortedMap.of();
      _virtualAddresses = ImmutableSet.of();
    }

    public @Nonnull HsrpGroup build() {
      return new HsrpGroup(
          _authentication,
          _helloTime,
          _holdTime,
          _preempt,
          _priority,
          _sourceAddress,
          _trackActions,
          _virtualAddresses);
    }

    public @Nonnull Builder setAuthentication(@Nullable String authentication) {
      _authentication = authentication;
      return this;
    }

    public @Nonnull Builder setHelloTime(int helloTime) {
      _helloTime = helloTime;
      return this;
    }

    public @Nonnull Builder setHoldTime(int holdTime) {
      _holdTime = holdTime;
      return this;
    }

    public @Nonnull Builder setPreempt(boolean preempt) {
      _preempt = preempt;
      return this;
    }

    public @Nonnull Builder setPriority(int priority) {
      _priority = priority;
      return this;
    }

    public @Nonnull Builder setSourceAddress(@Nullable ConcreteInterfaceAddress sourceAddress) {
      _sourceAddress = sourceAddress;
      return this;
    }

    public @Nonnull Builder setTrackActions(SortedMap<String, TrackAction> trackActions) {
      _trackActions = ImmutableSortedMap.copyOf(trackActions);
      return this;
    }

    public @Nonnull Builder setVirtualAddresses(Set<Ip> virtualAddresses) {
      _virtualAddresses = virtualAddresses;
      return this;
    }
  }

  public static final int DEFAULT_HELLO_TIME = 3000; // 3 seconds
  public static final int DEFAULT_HOLD_TIME = 10000; // 10 seconds
  public static final int DEFAULT_PRIORITY = 100;

  private static final String PROP_AUTHENTICATION = "authentication";
  private static final String PROP_HELLO_TIME = "helloTime";
  private static final String PROP_HOLD_TIME = "holdTime";
  private static final String PROP_PREEMPT = "preempt";
  private static final String PROP_PRIORITY = "priority";
  private static final String PROP_SOURCE_ADDRESS = "sourceAddress";
  private static final String PROP_TRACK_ACTIONS = "trackActions";
  private static final String PROP_VIRTUAL_ADDRESSES = "virtualAddresses";

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull HsrpGroup create(
      @JsonProperty(PROP_AUTHENTICATION) @Nullable String authentication,
      @JsonProperty(PROP_HELLO_TIME) @Nullable Integer helloTime,
      @JsonProperty(PROP_HOLD_TIME) @Nullable Integer holdTime,
      @JsonProperty(PROP_PREEMPT) @Nullable Boolean preempt,
      @JsonProperty(PROP_PRIORITY) @Nullable Integer priority,
      @JsonProperty(PROP_SOURCE_ADDRESS) @Nullable ConcreteInterfaceAddress sourceAddress,
      @JsonProperty(PROP_TRACK_ACTIONS) @Nullable SortedMap<String, TrackAction> trackActions,
      @JsonProperty(PROP_VIRTUAL_ADDRESSES) @Nullable Set<Ip> virtualAddresses) {
    return new HsrpGroup(
        authentication,
        requireNonNull(helloTime, String.format("Missing required property: %s", PROP_HELLO_TIME)),
        requireNonNull(holdTime, String.format("Missing required property: %s", PROP_HOLD_TIME)),
        requireNonNull(preempt, String.format("Missing required property: %s", PROP_PREEMPT)),
        requireNonNull(priority, String.format("Missing required property: %s", PROP_PRIORITY)),
        sourceAddress,
        trackActions != null ? ImmutableSortedMap.copyOf(trackActions) : ImmutableSortedMap.of(),
        firstNonNull(virtualAddresses, ImmutableSet.of()));
  }

  private final String _authentication;
  private final int _helloTime;
  private final int _holdTime;
  private final Set<Ip> _virtualAddresses;
  private final boolean _preempt;
  private final int _priority;
  private final @Nullable ConcreteInterfaceAddress _sourceAddress;
  private final SortedMap<String, TrackAction> _trackActions;

  private HsrpGroup(
      @Nullable String authentication,
      int helloTime,
      int holdTime,
      boolean preempt,
      int priority,
      @Nullable ConcreteInterfaceAddress sourceAddress,
      SortedMap<String, TrackAction> trackActions,
      Set<Ip> virtualAddresses) {
    _authentication = authentication;
    _helloTime = helloTime;
    _holdTime = holdTime;
    _virtualAddresses = ImmutableSortedSet.copyOf(virtualAddresses);
    _preempt = preempt;
    _priority = priority;
    _sourceAddress = sourceAddress;
    _trackActions = trackActions;
  }

  /** SHA256-hashed authentication string */
  @JsonProperty(PROP_AUTHENTICATION)
  public @Nullable String getAuthentication() {
    return _authentication;
  }

  /** The interval in milliseconds between hello messages */
  @JsonProperty(PROP_HELLO_TIME)
  public int getHelloTime() {
    return _helloTime;
  }

  /**
   * The timeout in milliseconds after the last received hello message before another router is
   * assumed to be down
   */
  @JsonProperty(PROP_HOLD_TIME)
  public int getHoldTime() {
    return _holdTime;
  }

  /** Whether this router should preempt the active router when its priority is higher */
  @JsonProperty(PROP_PREEMPT)
  public boolean getPreempt() {
    return _preempt;
  }

  /** The priority when electing the active HSRP router. Higher is more preferred. */
  @JsonProperty(PROP_PRIORITY)
  public int getPriority() {
    return _priority;
  }

  /** The address from which HSRP control traffic is sent. */
  @JsonProperty(PROP_SOURCE_ADDRESS)
  public @Nullable ConcreteInterfaceAddress getSourceAddress() {
    return _sourceAddress;
  }

  /** The HSRP standby IP addresses to assume when this is the active router in the group */
  @JsonProperty(PROP_VIRTUAL_ADDRESSES)
  public @Nonnull Set<Ip> getVirtualAddresses() {
    return _virtualAddresses;
  }

  /**
   * Mapping: trackingGroupID -&gt; trackAction
   *
   * <p>When a tracking group identified by a key is down, the action in the corresponding value is
   * taken.
   */
  @JsonProperty(PROP_TRACK_ACTIONS)
  public @Nonnull SortedMap<String, TrackAction> getTrackActions() {
    return _trackActions;
  }

  public @Nonnull Builder toBuilder() {
    return builder()
        .setAuthentication(_authentication)
        .setHelloTime(_helloTime)
        .setHoldTime(_holdTime)
        .setPreempt(_preempt)
        .setPriority(_priority)
        .setTrackActions(_trackActions)
        .setVirtualAddresses(_virtualAddresses);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HsrpGroup)) {
      return false;
    }
    HsrpGroup rhs = (HsrpGroup) obj;
    return Objects.equals(_authentication, rhs._authentication)
        && _helloTime == rhs._helloTime
        && _holdTime == rhs._holdTime
        && _preempt == rhs._preempt
        && _priority == rhs._priority
        && Objects.equals(_sourceAddress, rhs._sourceAddress)
        && _virtualAddresses.equals(rhs._virtualAddresses)
        && _trackActions.equals(rhs._trackActions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _authentication,
        _helloTime,
        _holdTime,
        _preempt,
        _priority,
        _sourceAddress,
        _trackActions,
        _virtualAddresses);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_AUTHENTICATION, _authentication)
        .add(PROP_HELLO_TIME, _helloTime)
        .add(PROP_HOLD_TIME, _holdTime)
        .add(PROP_PREEMPT, _preempt)
        .add(PROP_PRIORITY, _priority)
        .add(PROP_SOURCE_ADDRESS, _sourceAddress)
        .add(PROP_TRACK_ACTIONS, _trackActions)
        .add(PROP_VIRTUAL_ADDRESSES, _virtualAddresses)
        .toString();
  }
}
