package org.batfish.representation.cisco_xr;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LineAction;

@ParametersAreNonnullByDefault
public class Ipv4AccessListLine implements Serializable {

  public static class Builder {

    @Nullable private LineAction _action;
    @Nullable private AccessListAddressSpecifier _dstAddressSpecifier;
    @Nullable private String _name;
    @Nullable private Ipv4Nexthop _nexthop1;
    @Nullable private Ipv4Nexthop _nexthop2;
    @Nullable private Ipv4Nexthop _nexthop3;
    @Nullable private AccessListServiceSpecifier _serviceSpecifier;
    @Nullable private Long _seq;
    @Nullable private AccessListAddressSpecifier _srcAddressSpecifier;

    private Builder() {}

    public Ipv4AccessListLine build() {
      return new Ipv4AccessListLine(this);
    }

    public Builder setAction(LineAction action) {
      _action = action;
      return this;
    }

    public Builder setDstAddressSpecifier(AccessListAddressSpecifier dstAddressSpecifier) {
      _dstAddressSpecifier = dstAddressSpecifier;
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }

    public Builder setNexthop1(Ipv4Nexthop nexthop) {
      _nexthop1 = nexthop;
      return this;
    }

    public Builder setNexthop2(Ipv4Nexthop nexthop) {
      _nexthop2 = nexthop;
      return this;
    }

    public Builder setNexthop3(Ipv4Nexthop nexthop) {
      _nexthop3 = nexthop;
      return this;
    }

    public Builder setServiceSpecifier(AccessListServiceSpecifier serviceSpecifier) {
      _serviceSpecifier = serviceSpecifier;
      return this;
    }

    public Builder setSeq(long seq) {
      _seq = seq;
      return this;
    }

    public Builder setSrcAddressSpecifier(AccessListAddressSpecifier srcAddressSpecifier) {
      _srcAddressSpecifier = srcAddressSpecifier;
      return this;
    }
  }

  @Nonnull
  public static Builder builder() {
    return new Builder();
  }

  private final long _seq;
  @Nonnull private final LineAction _action;
  @Nonnull private final String _name;
  @Nonnull private final AccessListAddressSpecifier _srcAddressSpecifier;
  @Nonnull private final AccessListAddressSpecifier _dstAddressSpecifier;
  @Nonnull private final AccessListServiceSpecifier _serviceSpecifier;
  @Nullable private final Ipv4Nexthop _nexthop1;
  @Nullable private final Ipv4Nexthop _nexthop2;
  @Nullable private final Ipv4Nexthop _nexthop3;

  private Ipv4AccessListLine(Builder builder) {
    _action = requireNonNull(builder._action);
    _seq = requireNonNull(builder._seq);
    _dstAddressSpecifier = requireNonNull(builder._dstAddressSpecifier);
    _name = requireNonNull(builder._name);
    _serviceSpecifier = requireNonNull(builder._serviceSpecifier);
    _srcAddressSpecifier = requireNonNull(builder._srcAddressSpecifier);
    _nexthop1 = builder._nexthop1;
    _nexthop2 = builder._nexthop2;
    _nexthop3 = builder._nexthop3;
  }

  @Nonnull
  public LineAction getAction() {
    return _action;
  }

  @Nonnull
  public AccessListAddressSpecifier getDestinationAddressSpecifier() {
    return _dstAddressSpecifier;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public Ipv4Nexthop getNexthop1() {
    return _nexthop1;
  }

  @Nullable
  public Ipv4Nexthop getNexthop2() {
    return _nexthop2;
  }

  @Nullable
  public Ipv4Nexthop getNexthop3() {
    return _nexthop3;
  }

  @Nonnull
  public AccessListServiceSpecifier getServiceSpecifier() {
    return _serviceSpecifier;
  }

  public long getSeq() {
    return _seq;
  }

  @Nonnull
  public AccessListAddressSpecifier getSourceAddressSpecifier() {
    return _srcAddressSpecifier;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("action", _action)
        .add("dstAddressSpecicier", _dstAddressSpecifier)
        .add("name", _name)
        .add("serviceSpecifier", _serviceSpecifier)
        .add("srcAddressSpecifier", _srcAddressSpecifier)
        .toString();
  }
}
