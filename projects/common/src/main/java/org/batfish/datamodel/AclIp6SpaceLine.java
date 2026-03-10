package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.ObjectStreamException;
import java.io.Serializable;
import javax.annotation.Nonnull;

/** A line in an {@link AclIp6Space}. */
public class AclIp6SpaceLine implements Comparable<AclIp6SpaceLine>, Serializable {
  // Soft values: let them be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  private static final LoadingCache<Ip6Space, AclIp6SpaceLine> PERMIT_CACHE =
      Caffeine.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(s -> new AclIp6SpaceLine(s, LineAction.PERMIT));
  private static final LoadingCache<Ip6Space, AclIp6SpaceLine> REJECT_CACHE =
      Caffeine.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(s -> new AclIp6SpaceLine(s, LineAction.DENY));

  public static class Builder {

    private LineAction _action;

    private Ip6Space _ip6Space;

    private Builder() {
      _action = LineAction.PERMIT;
    }

    public AclIp6SpaceLine build() {
      return create(_ip6Space, _action);
    }

    public Builder setAction(@Nonnull LineAction action) {
      _action = action;
      return this;
    }

    public Builder setIp6Space(@Nonnull Ip6Space ip6Space) {
      _ip6Space = ip6Space;
      return this;
    }
  }

  public static final AclIp6SpaceLine DENY_ALL = AclIp6SpaceLine.reject(UniverseIp6Space.INSTANCE);

  public static final AclIp6SpaceLine PERMIT_ALL =
      AclIp6SpaceLine.permit(UniverseIp6Space.INSTANCE);
  private static final String PROP_ACTION = "action";
  private static final String PROP_IP6_SPACE = "ip6Space";

  public static Builder builder() {
    return new Builder();
  }

  public static AclIp6SpaceLine permit(Ip6Space ip6Space) {
    return builder().setIp6Space(ip6Space).build();
  }

  public static AclIp6SpaceLine reject(Ip6Space ip6Space) {
    return builder().setIp6Space(ip6Space).setAction(LineAction.DENY).build();
  }

  private final @Nonnull LineAction _action;

  private final @Nonnull Ip6Space _ip6Space;

  @JsonCreator
  private static AclIp6SpaceLine jsonCreator(
      @JsonProperty(PROP_IP6_SPACE) @Nonnull Ip6Space ip6Space,
      @JsonProperty(PROP_ACTION) @Nonnull LineAction action) {
    return create(ip6Space, action);
  }

  private static AclIp6SpaceLine create(@Nonnull Ip6Space space, @Nonnull LineAction action) {
    return action == LineAction.PERMIT ? PERMIT_CACHE.get(space) : REJECT_CACHE.get(space);
  }

  @VisibleForTesting
  AclIp6SpaceLine(@Nonnull Ip6Space space, @Nonnull LineAction action) {
    _ip6Space = space;
    _action = action;
  }

  @Override
  public int compareTo(@Nonnull AclIp6SpaceLine o) {
    int actionComparison = _action.compareTo(o._action);
    if (actionComparison != 0) {
      return actionComparison;
    }
    return _ip6Space.compareTo(o._ip6Space);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof AclIp6SpaceLine)) {
      return false;
    }
    AclIp6SpaceLine rhs = (AclIp6SpaceLine) o;
    return _action == rhs._action && _ip6Space.equals(rhs._ip6Space);
  }

  @JsonProperty(PROP_ACTION)
  public @Nonnull LineAction getAction() {
    return _action;
  }

  @JsonProperty(PROP_IP6_SPACE)
  public @Nonnull Ip6Space getIp6Space() {
    return _ip6Space;
  }

  @Override
  public int hashCode() {
    return 31 * _action.ordinal() + _ip6Space.hashCode();
  }

  public Builder toBuilder() {
    return builder().setAction(_action).setIp6Space(_ip6Space);
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    if (_action != LineAction.PERMIT) {
      helper.add(PROP_ACTION, _action);
    }
    helper.add(PROP_IP6_SPACE, _ip6Space);
    return helper.toString();
  }

  /** Re-intern after deserialization. */
  private Object readResolve() throws ObjectStreamException {
    return create(_ip6Space, _action);
  }
}
