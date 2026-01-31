package org.batfish.vendor.arista.representation;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

public class ExtendedAccessListLine implements Serializable {

  public static class Builder {

    private LineAction _action;
    private AccessListAddressSpecifier _dstAddressSpecifier;
    private String _name;
    private AccessListServiceSpecifier _serviceSpecifier;
    private AccessListAddressSpecifier _srcAddressSpecifier;

    private Builder() {}

    public ExtendedAccessListLine build() {
      return new ExtendedAccessListLine(this);
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

  private final @Nonnull LineAction _action;
  private final @Nonnull AccessListAddressSpecifier _dstAddressSpecifier;
  private final @Nonnull String _name;
  private final @Nonnull AccessListServiceSpecifier _serviceSpecifier;
  private final @Nonnull AccessListAddressSpecifier _srcAddressSpecifier;

  private ExtendedAccessListLine(Builder builder) {
    _action = requireNonNull(builder._action);
    _dstAddressSpecifier = requireNonNull(builder._dstAddressSpecifier);
    _name = requireNonNull(builder._name);
    _serviceSpecifier = requireNonNull(builder._serviceSpecifier);
    _srcAddressSpecifier = requireNonNull(builder._srcAddressSpecifier);
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull AccessListAddressSpecifier getDestinationAddressSpecifier() {
    return _dstAddressSpecifier;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull AccessListServiceSpecifier getServiceSpecifier() {
    return _serviceSpecifier;
  }

  public @Nonnull AccessListAddressSpecifier getSourceAddressSpecifier() {
    return _srcAddressSpecifier;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("action", _action)
        .add("dstAddressSpecifier", _dstAddressSpecifier)
        .add("name", _name)
        .add("serviceSpecifier", _serviceSpecifier)
        .add("srcAddressSpecifier", _srcAddressSpecifier)
        .toString();
  }
}
