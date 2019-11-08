package org.batfish.representation.cisco_xr;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

public class Ipv4AccessListLine implements Serializable {

  public static class Builder {

    private LineAction _action;

    private AccessListAddressSpecifier _dstAddressSpecifier;

    private String _name;

    private AccessListServiceSpecifier _serviceSpecifier;

    private AccessListAddressSpecifier _srcAddressSpecifier;

    private Builder() {}

    public Ipv4AccessListLine build() {
      return new Ipv4AccessListLine(this);
    }

    public Builder setAction(@Nonnull LineAction action) {
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

    public Builder setServiceSpecifier(AccessListServiceSpecifier serviceSpecifier) {
      _serviceSpecifier = serviceSpecifier;
      return this;
    }

    public Builder setSrcAddressSpecifier(AccessListAddressSpecifier srcAddressSpecifier) {
      _srcAddressSpecifier = srcAddressSpecifier;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final LineAction _action;

  private final AccessListAddressSpecifier _dstAddressSpecifier;

  private final String _name;

  private final AccessListServiceSpecifier _serviceSpecifier;

  private final AccessListAddressSpecifier _srcAddressSpecifier;

  private Ipv4AccessListLine(Builder builder) {
    _action = requireNonNull(builder._action);
    _dstAddressSpecifier = requireNonNull(builder._dstAddressSpecifier);
    _name = requireNonNull(builder._name);
    _serviceSpecifier = requireNonNull(builder._serviceSpecifier);
    _srcAddressSpecifier = requireNonNull(builder._srcAddressSpecifier);
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public AccessListAddressSpecifier getDestinationAddressSpecifier() {
    return _dstAddressSpecifier;
  }

  public String getName() {
    return _name;
  }

  public AccessListServiceSpecifier getServiceSpecifier() {
    return _serviceSpecifier;
  }

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
