package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Objects;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** An HSRP group defined on an {@link Interface} */
public final class HsrpGroup implements Serializable {

  public static final class Builder {

    private String _authentication;

    private int _helloTime;

    private int _holdTime;

    private Ip _ip;

    private Integer _number;

    private boolean _preempt;

    private int _priority;

    private SortedMap<String, TrackAction> _trackActions;

    private Builder() {
      _holdTime = DEFAULT_HOLD_TIME;
      _priority = DEFAULT_PRIORITY;
      _trackActions = ImmutableSortedMap.of();
    }

    public @Nonnull HsrpGroup build() {
      return new HsrpGroup(
          _authentication,
          _helloTime,
          _holdTime,
          _ip,
          requireNonNull(_number, "Must set HSRP group number"),
          _preempt,
          _priority,
          _trackActions);
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

    public @Nonnull Builder setIp(@Nullable Ip ip) {
      _ip = ip;
      return this;
    }

    public @Nonnull Builder setNumber(int number) {
      _number = number;
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

    public @Nonnull Builder setTrackActions(@Nonnull SortedMap<String, TrackAction> trackActions) {
      _trackActions = ImmutableSortedMap.copyOf(trackActions);
      return this;
    }
  }

  public static final int DEFAULT_HELLO_TIME = 3000; // 3 seconds

  public static final int DEFAULT_HOLD_TIME = 10000; // 10 seconds

  public static final int DEFAULT_PRIORITY = 100;

  private static final String PROP_AUTHENTICATION = "authentication";

  private static final String PROP_HELLO_TIME = "helloTime";

  private static final String PROP_HOLD_TIME = "holdTime";

  private static final String PROP_IP = "ip";

  private static final String PROP_NUMBER = "number";

  private static final String PROP_PREEMPT = "preempt";

  private static final String PROP_PRIORITY = "priority";

  private static final String PROP_TRACK_ACTIONS = "trackActions";

  private static final long serialVersionUID = 1L;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull HsrpGroup create(
      @JsonProperty(PROP_AUTHENTICATION) String authentication,
      @JsonProperty(PROP_HELLO_TIME) int helloTime,
      @JsonProperty(PROP_HOLD_TIME) int holdTime,
      @JsonProperty(PROP_IP) Ip ip,
      @JsonProperty(PROP_NUMBER) int number,
      @JsonProperty(PROP_PREEMPT) boolean preempt,
      @JsonProperty(PROP_PRIORITY) int priority,
      @JsonProperty(PROP_TRACK_ACTIONS) SortedMap<String, TrackAction> trackActions) {
    return new HsrpGroup(
        authentication,
        helloTime,
        holdTime,
        ip,
        number,
        preempt,
        priority,
        trackActions != null ? ImmutableSortedMap.copyOf(trackActions) : ImmutableSortedMap.of());
  }

  private final String _authentication;

  private final int _helloTime;

  private final int _holdTime;

  private final Ip _ip;

  private final int _number;

  private final boolean _preempt;

  private final int _priority;

  private final SortedMap<String, TrackAction> _trackActions;

  private HsrpGroup(
      @Nullable String authentication,
      int helloTime,
      int holdTime,
      @Nullable Ip ip,
      int number,
      boolean preempt,
      int priority,
      @Nonnull SortedMap<String, TrackAction> trackActions) {
    _authentication = authentication;
    _helloTime = helloTime;
    _holdTime = holdTime;
    _ip = ip;
    _number = number;
    _preempt = preempt;
    _priority = priority;
    _trackActions = trackActions;
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
        && Objects.equals(_ip, rhs._ip)
        && _number == rhs._number
        && _preempt == rhs._preempt
        && _priority == rhs._priority
        && _trackActions.equals(rhs._trackActions);
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

  /** The HSRP standby IP address to assume when this is the active router in the group */
  @JsonProperty(PROP_IP)
  public @Nullable Ip getIp() {
    return _ip;
  }

  /** The shared number identifying the HSRP group in which this interface is a participant */
  @JsonProperty(PROP_NUMBER)
  public int getNumber() {
    return _number;
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

  /**
   * Mapping: trackingGroupID -> trackAction<br>
   * When a tracking group identified by a key is down, the action in the corresponding value is
   * taken.
   */
  @JsonProperty(PROP_TRACK_ACTIONS)
  public @Nonnull SortedMap<String, TrackAction> getTrackActions() {
    return _trackActions;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _authentication, _helloTime, _holdTime, _ip, _number, _preempt, _priority, _trackActions);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_AUTHENTICATION, _authentication)
        .add(PROP_HELLO_TIME, _helloTime)
        .add(PROP_HOLD_TIME, _holdTime)
        .add(PROP_IP, _ip)
        .add(PROP_NUMBER, _number)
        .add(PROP_PREEMPT, _preempt)
        .add(PROP_PRIORITY, _priority)
        .add(PROP_TRACK_ACTIONS, _trackActions)
        .toString();
  }
}
