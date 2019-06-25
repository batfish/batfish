package org.batfish.datamodel.hsrp;

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
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.tracking.TrackAction;

/** An HSRP group defined on an {@link Interface} */
public final class HsrpGroup implements Serializable {

  public static final class Builder {

    private String _authentication;

    private Integer _groupNumber;

    private int _helloTime;

    private int _holdTime;

    private Ip _ip;

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
          requireNonNull(_groupNumber, "Must set HSRP group number via setGroupNumber"),
          _helloTime,
          _holdTime,
          _ip,
          _preempt,
          _priority,
          _trackActions);
    }

    public @Nonnull Builder setAuthentication(@Nullable String authentication) {
      _authentication = authentication;
      return this;
    }

    public @Nonnull Builder setGroupNumber(int groupNumber) {
      _groupNumber = groupNumber;
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
  private static final String PROP_GROUP_NUMBER = "groupNumber";
  private static final String PROP_HELLO_TIME = "helloTime";
  private static final String PROP_HOLD_TIME = "holdTime";
  private static final String PROP_IP = "ip";
  private static final String PROP_PREEMPT = "preempt";
  private static final String PROP_PRIORITY = "priority";
  private static final String PROP_TRACK_ACTIONS = "trackActions";

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull HsrpGroup create(
      @JsonProperty(PROP_AUTHENTICATION) String authentication,
      @JsonProperty(PROP_GROUP_NUMBER) Integer groupNumber,
      @JsonProperty(PROP_HELLO_TIME) Integer helloTime,
      @JsonProperty(PROP_HOLD_TIME) Integer holdTime,
      @JsonProperty(PROP_IP) Ip ip,
      @JsonProperty(PROP_PREEMPT) Boolean preempt,
      @JsonProperty(PROP_PRIORITY) Integer priority,
      @JsonProperty(PROP_TRACK_ACTIONS) SortedMap<String, TrackAction> trackActions) {
    return new HsrpGroup(
        authentication,
        requireNonNull(
            groupNumber, String.format("Missing required property: %s", PROP_GROUP_NUMBER)),
        requireNonNull(helloTime, String.format("Missing required property: %s", PROP_HELLO_TIME)),
        requireNonNull(holdTime, String.format("Missing required property: %s", PROP_HOLD_TIME)),
        ip,
        requireNonNull(preempt, String.format("Missing required property: %s", PROP_PREEMPT)),
        requireNonNull(priority, String.format("Missing required property: %s", PROP_PRIORITY)),
        trackActions != null ? ImmutableSortedMap.copyOf(trackActions) : ImmutableSortedMap.of());
  }

  private final String _authentication;

  private final int _groupNumber;

  private final int _helloTime;

  private final int _holdTime;

  private final Ip _ip;

  private final boolean _preempt;

  private final int _priority;

  private final SortedMap<String, TrackAction> _trackActions;

  private HsrpGroup(
      @Nullable String authentication,
      int groupNumber,
      int helloTime,
      int holdTime,
      @Nullable Ip ip,
      boolean preempt,
      int priority,
      @Nonnull SortedMap<String, TrackAction> trackActions) {
    _authentication = authentication;
    _groupNumber = groupNumber;
    _helloTime = helloTime;
    _holdTime = holdTime;
    _ip = ip;
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
        && _groupNumber == rhs._groupNumber
        && _helloTime == rhs._helloTime
        && _holdTime == rhs._holdTime
        && Objects.equals(_ip, rhs._ip)
        && _preempt == rhs._preempt
        && _priority == rhs._priority
        && _trackActions.equals(rhs._trackActions);
  }

  /** SHA256-hashed authentication string */
  @JsonProperty(PROP_AUTHENTICATION)
  public @Nullable String getAuthentication() {
    return _authentication;
  }

  /** The shared number identifying the HSRP group in which this interface is a participant */
  @JsonProperty(PROP_GROUP_NUMBER)
  public int getGroupNumber() {
    return _groupNumber;
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
   * Mapping: trackingGroupID -&gt; trackAction
   *
   * <p>When a tracking group identified by a key is down, the action in the corresponding value is
   * taken.
   */
  @JsonProperty(PROP_TRACK_ACTIONS)
  public @Nonnull SortedMap<String, TrackAction> getTrackActions() {
    return _trackActions;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _authentication,
        _groupNumber,
        _helloTime,
        _holdTime,
        _ip,
        _preempt,
        _priority,
        _trackActions);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_AUTHENTICATION, _authentication)
        .add(PROP_GROUP_NUMBER, _groupNumber)
        .add(PROP_HELLO_TIME, _helloTime)
        .add(PROP_HOLD_TIME, _holdTime)
        .add(PROP_IP, _ip)
        .add(PROP_PREEMPT, _preempt)
        .add(PROP_PRIORITY, _priority)
        .add(PROP_TRACK_ACTIONS, _trackActions)
        .toString();
  }
}
