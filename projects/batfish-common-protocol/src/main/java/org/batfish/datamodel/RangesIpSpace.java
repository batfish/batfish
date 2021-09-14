package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Range;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * An {@link IpSpace} that is a set of ranges of IP addresses. Suitable for collections of {@link
 * Ip}, {@link Prefix}, and/or ranges of {@link Ip} addresses (e.g., as in {@link
 * org.batfish.datamodel.transformation.AssignIpAddressFromPool}).
 */
public final class RangesIpSpace extends IpSpace {
  public static Builder builder() {
    return new Builder();
  }

  public boolean containsIp(Ip ip) {
    return _space.contains(ip.asLong());
  }

  @Override
  public @Nonnull IpSpace complement() {
    if (isEmpty()) {
      return UniverseIpSpace.INSTANCE;
    }
    LongSpace complement = LongSpace.builder().including(ALL).excluding(_space).build();
    return create(complement);
  }

  public static @Nonnull RangesIpSpace empty() {
    return EMPTY;
  }

  @JsonProperty(PROP_SPACE)
  public LongSpace getSpace() {
    return _space;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return _space.isEmpty();
  }

  public static @Nonnull RangesIpSpace union(RangesIpSpace left, RangesIpSpace right) {
    return RangesIpSpace.create(
        LongSpace.builder().including(left._space).including(right._space).build());
  }

  public static class Builder {
    private final @Nonnull LongSpace.Builder _builder;

    private Builder() {
      _builder = LongSpace.builder();
    }

    public Builder excluding(Ip ip) {
      _builder.excluding(ip.asLong());
      return this;
    }

    public Builder excluding(Prefix prefix) {
      _builder.excluding(prefixRange(prefix));
      return this;
    }

    public Builder excluding(RangesIpSpace space) {
      _builder.excluding(space._space);
      return this;
    }

    public Builder including(Ip ip) {
      _builder.including(ip.asLong());
      return this;
    }

    public Builder including(Prefix prefix) {
      _builder.including(prefixRange(prefix));
      return this;
    }

    public Builder including(RangesIpSpace space) {
      _builder.including(space._space);
      return this;
    }

    public RangesIpSpace build() {
      return create(_builder.build());
    }
  }

  // Internal impl details

  @JsonCreator
  @VisibleForTesting
  static RangesIpSpace create(@JsonProperty(PROP_SPACE) LongSpace space) {
    return new RangesIpSpace(space);
  }

  private RangesIpSpace(LongSpace space) {
    _space = space;
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitRangesIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    RangesIpSpace other = (RangesIpSpace) o;
    if (_space.equals(other._space)) {
      return 0;
    }
    LongSpace onlyThis = LongSpace.builder().including(_space).excluding(other._space).build();
    if (onlyThis.isEmpty()) {
      return -1;
    }
    LongSpace onlyOther = LongSpace.builder().including(other._space).excluding(_space).build();
    if (onlyOther.isEmpty()) {
      return 1;
    }
    return onlyThis.least().compareTo(onlyOther.least());
  }

  private static Range<Long> prefixRange(Prefix p) {
    return Range.closed(p.getStartIp().asLong(), p.getEndIp().asLong());
  }

  @Override
  protected boolean exprEquals(Object o) {
    return _space.equals(((RangesIpSpace) o)._space);
  }

  @Override
  public int hashCode() {
    return _space.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_SPACE, _space).toString();
  }

  private static final String PROP_SPACE = "space";
  private final LongSpace _space;
  private static final Range<Long> ALL = prefixRange(Prefix.ZERO);
  private static final RangesIpSpace EMPTY = create(LongSpace.EMPTY);
}
