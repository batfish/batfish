package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;

public class AclIpSpaceLine implements Comparable<AclIpSpaceLine>, Serializable {
  // Soft values: let them be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (16 bytes seems smallest possible entry (2 pointers), would be 16 MiB total).
  private static final LoadingCache<IpSpace, AclIpSpaceLine> PERMIT_CACHE =
      Caffeine.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(s -> new AclIpSpaceLine(s, LineAction.PERMIT));
  private static final LoadingCache<IpSpace, AclIpSpaceLine> REJECT_CACHE =
      Caffeine.newBuilder()
          .softValues()
          .maximumSize(1 << 20)
          .build(s -> new AclIpSpaceLine(s, LineAction.DENY));

  public static class Builder {

    private LineAction _action;

    private IpSpace _ipSpace;

    private Builder() {
      _action = LineAction.PERMIT;
    }

    public AclIpSpaceLine build() {
      return create(_ipSpace, _action);
    }

    public Builder setAction(@Nonnull LineAction action) {
      _action = action;
      return this;
    }

    public Builder setIpSpace(@Nonnull IpSpace ipSpace) {
      _ipSpace = ipSpace;
      return this;
    }
  }

  public static final AclIpSpaceLine DENY_ALL = AclIpSpaceLine.reject(UniverseIpSpace.INSTANCE);

  public static final AclIpSpaceLine PERMIT_ALL = AclIpSpaceLine.permit(UniverseIpSpace.INSTANCE);
  private static final String PROP_ACTION = "action";
  private static final String PROP_IP_SPACE = "ipSpace";
  private static final String PROP_DEPRECATED_SRC_TEXT = "srcText";

  public static Builder builder() {
    return new Builder();
  }

  public static AclIpSpaceLine permit(IpSpace ipSpace) {
    return builder().setIpSpace(ipSpace).build();
  }

  public static AclIpSpaceLine reject(IpSpace ipSpace) {
    return builder().setIpSpace(ipSpace).setAction(LineAction.DENY).build();
  }

  private final @Nonnull LineAction _action;

  private final @Nonnull IpSpace _ipSpace;

  @JsonCreator
  private static AclIpSpaceLine jsonCreator(
      @JsonProperty(PROP_IP_SPACE) @Nonnull IpSpace ipSpace,
      @JsonProperty(PROP_ACTION) @Nonnull LineAction action,
      @JsonProperty(PROP_DEPRECATED_SRC_TEXT) String unused) {
    return create(ipSpace, action);
  }

  private static AclIpSpaceLine create(@Nonnull IpSpace space, @Nonnull LineAction action) {
    return action == LineAction.PERMIT ? PERMIT_CACHE.get(space) : REJECT_CACHE.get(space);
  }

  @VisibleForTesting
  AclIpSpaceLine(@Nonnull IpSpace space, @Nonnull LineAction action) {
    _ipSpace = space;
    _action = action;
  }

  @Override
  public int compareTo(@Nonnull AclIpSpaceLine o) {
    return Comparator.comparing(AclIpSpaceLine::getAction)
        .thenComparing(AclIpSpaceLine::getIpSpace)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof AclIpSpaceLine)) {
      return false;
    }
    AclIpSpaceLine rhs = (AclIpSpaceLine) o;
    return _action == rhs._action && _ipSpace.equals(rhs._ipSpace);
  }

  @JsonProperty(PROP_ACTION)
  public @Nonnull LineAction getAction() {
    return _action;
  }

  @JsonProperty(PROP_IP_SPACE)
  public @Nonnull IpSpace getIpSpace() {
    return _ipSpace;
  }

  @Override
  public int hashCode() {
    return 31 * _action.ordinal() + _ipSpace.hashCode();
  }

  public Builder toBuilder() {
    return builder().setAction(_action).setIpSpace(_ipSpace);
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    helper.add(PROP_ACTION, _action);
    helper.add(PROP_IP_SPACE, _ipSpace);
    return helper.toString();
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return create(_ipSpace, _action);
  }
}
