package org.batfish.vendor.cisco_nxos.representation;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Range;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** An IPv4 static route */
public final class StaticRoute implements Serializable {

  /** Uniquely identifies a static route. */
  public static final class StaticRouteKey implements Serializable {
    private final boolean _discard;
    private final @Nullable String _nextHopInterface;
    private final @Nullable Ip _nextHopIp;
    private final @Nullable String _nextHopVrf;
    private final @Nonnull Prefix _prefix;

    public StaticRouteKey(
        Prefix prefix,
        boolean discard,
        @Nullable String nextHopInterface,
        @Nullable Ip nextHopIp,
        @Nullable String nextHopVrf) {
      checkNextHopFields(discard, nextHopInterface, nextHopIp, nextHopVrf);
      _discard = discard;
      _nextHopInterface = nextHopInterface;
      _nextHopIp = nextHopIp;
      _nextHopVrf = nextHopVrf;
      _prefix = prefix;
    }

    /** Creates a {@link StaticRoute} with this key's properties. Non-key properties are not set. */
    public StaticRoute toRoute() {
      return StaticRoute.builder()
          .setDiscard(_discard)
          .setNextHopInterface(_nextHopInterface)
          .setNextHopIp(_nextHopIp)
          .setNextHopVrf(_nextHopVrf)
          .setPrefix(_prefix)
          .build();
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof StaticRouteKey)) {
        return false;
      }
      StaticRouteKey that = (StaticRouteKey) o;
      return _discard == that._discard
          && Objects.equals(_nextHopInterface, that._nextHopInterface)
          && Objects.equals(_nextHopIp, that._nextHopIp)
          && Objects.equals(_nextHopVrf, that._nextHopVrf)
          && _prefix.equals(that._prefix);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_discard, _nextHopInterface, _nextHopIp, _nextHopVrf, _prefix);
    }
  }

  public static final class Builder {

    private boolean _discard;
    private @Nullable String _name;
    private @Nullable String _nextHopInterface;
    private @Nullable Ip _nextHopIp;
    private @Nullable String _nextHopVrf;
    private int _preference;
    private @Nonnull Prefix _prefix;
    private long _tag;
    private @Nullable Integer _track;

    private Builder() {
      _preference = 1;
    }

    public @Nonnull StaticRoute build() {
      checkNextHopFields(_discard, _nextHopInterface, _nextHopIp, _nextHopVrf);
      checkArgument(
          STATIC_ROUTE_PREFERENCE_RANGE.contains(_preference),
          "Invalid preference %s outside of %s",
          _preference,
          STATIC_ROUTE_PREFERENCE_RANGE);
      checkArgument(
          0 <= _tag && _tag <= 0xFFFFFFFFL,
          "Invalid tag %s is not an unsigned 32-bit integer",
          _tag);
      return new StaticRoute(
          _discard,
          _name,
          _nextHopInterface,
          _nextHopIp,
          _nextHopVrf,
          _preference,
          _prefix,
          _track,
          _tag);
    }

    public @Nonnull Builder setDiscard(boolean discard) {
      _discard = discard;
      return this;
    }

    public @Nonnull Builder setName(@Nullable String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setNextHopInterface(@Nullable String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
      return this;
    }

    public @Nonnull Builder setNextHopIp(@Nullable Ip nextHopIp) {
      _nextHopIp = nextHopIp;
      return this;
    }

    public @Nonnull Builder setNextHopVrf(@Nullable String nextHopVrf) {
      _nextHopVrf = nextHopVrf;
      return this;
    }

    public @Nonnull Builder setPreference(int preference) {
      _preference = preference;
      return this;
    }

    public @Nonnull Builder setPrefix(Prefix prefix) {
      _prefix = prefix;
      return this;
    }

    public @Nonnull Builder setTag(long tag) {
      _tag = tag;
      return this;
    }

    public @Nonnull Builder setTrack(@Nullable Integer track) {
      _track = track;
      return this;
    }
  }

  /**
   * Checks that the given set of next hop properties are compatible.
   *
   * @throws IllegalArgumentException if next hop properties are incompatible
   */
  private static void checkNextHopFields(
      boolean discard,
      @Nullable String nextHopInterface,
      @Nullable Ip nextHopIp,
      @Nullable String nextHopVrf) {
    checkArgument(
        discard || nextHopInterface != null || nextHopIp != null,
        "Must specify either discard or next-hop options");
    checkArgument(
        !discard || (nextHopInterface == null && nextHopIp == null && nextHopVrf == null),
        "Discard static route mutually exclusive with next-hop options");
  }

  public static final IntegerSpace STATIC_ROUTE_PREFERENCE_RANGE =
      IntegerSpace.of(Range.closed(1, 255));
  public static final int MAX_NAME_LENGTH = 50;

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof StaticRoute)) {
      return false;
    }
    StaticRoute sr = (StaticRoute) o;
    return _discard == sr._discard
        && _preference == sr._preference
        && _tag == sr._tag
        && Objects.equals(_name, sr._name)
        && Objects.equals(_nextHopInterface, sr._nextHopInterface)
        && Objects.equals(_nextHopIp, sr._nextHopIp)
        && Objects.equals(_nextHopVrf, sr._nextHopVrf)
        && Objects.equals(_prefix, sr._prefix)
        && Objects.equals(_track, sr._track);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _discard,
        _name,
        _nextHopInterface,
        _nextHopIp,
        _nextHopVrf,
        _preference,
        _prefix,
        _tag,
        _track);
  }

  private final boolean _discard;
  private @Nullable String _name;
  private final @Nullable String _nextHopInterface;
  private final @Nullable Ip _nextHopIp;
  private final @Nullable String _nextHopVrf;
  private int _preference;
  private final @Nonnull Prefix _prefix;
  private long _tag;
  private @Nullable Integer _track;

  private StaticRoute(
      boolean discard,
      @Nullable String name,
      @Nullable String nextHopInterface,
      @Nullable Ip nextHopIp,
      @Nullable String nextHopVrf,
      int preference,
      Prefix prefix,
      @Nullable Integer track,
      long tag) {
    _prefix = prefix;
    _discard = discard;
    _nextHopInterface = nextHopInterface;
    _nextHopIp = nextHopIp;
    _nextHopVrf = nextHopVrf;
    _track = track;
    _name = name;
    _preference = preference;
    _tag = tag;
  }

  public boolean getDiscard() {
    return _discard;
  }

  public @Nullable String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  /**
   * The interface used for ARP lookup and forwarding. If not {@code null}, must be a member of
   * {@link #getNextHopVrf}.
   */
  public @Nullable String getNextHopInterface() {
    return _nextHopInterface;
  }

  public @Nullable Ip getNextHopIp() {
    return _nextHopIp;
  }

  /**
   * The {@link Vrf} used for lookup of the {@link #getNextHopIp}. To be effective, this {@link Vrf}
   * should be distinct from the VRF in which this route is installed.
   */
  public @Nullable String getNextHopVrf() {
    return _nextHopVrf;
  }

  public int getPreference() {
    return _preference;
  }

  public void setPreference(int preference) {
    _preference = preference;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  public long getTag() {
    return _tag;
  }

  public void setTag(long tag) {
    _tag = tag;
  }

  public @Nullable Integer getTrack() {
    return _track;
  }

  public void setTrack(int track) {
    _track = track;
  }
}
