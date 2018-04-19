package org.batfish.representation.cisco;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

public class ExtendedAccessListLine implements Serializable {

  public static class Builder {

    private LineAction _action;

    private ExtendedAccessListAddressSpecifier _dstAddressSpecifier;

    private String _name;

    private ExtendedAccessListServiceSpecifier _serviceSpecifier;

    private ExtendedAccessListAddressSpecifier _srcAddressSpecifier;

    private Builder() {
    }

    public ExtendedAccessListLine build() {
      return new ExtendedAccessListLine(this);
    }

    public Builder setAction(@Nonnull LineAction action) {
      _action = action;
      return this;
    }

    public Builder setDstAddressSpecifier(ExtendedAccessListAddressSpecifier dstAddressSpecifier) {
      _dstAddressSpecifier = dstAddressSpecifier;
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }

    public Builder setServiceSpecifier(ExtendedAccessListServiceSpecifier serviceSpecifier) {
      _serviceSpecifier = serviceSpecifier;
      return this;
    }

    public Builder setSrcAddressSpecifier(ExtendedAccessListAddressSpecifier srcAddressSpecifier) {
      _srcAddressSpecifier = srcAddressSpecifier;
      return this;
    }
  }

  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final LineAction _action;

  private final ExtendedAccessListAddressSpecifier _dstAddressSpecifier;

  private final String _name;

  private final ExtendedAccessListServiceSpecifier _serviceSpecifier;

  private final ExtendedAccessListAddressSpecifier _srcAddressSpecifier;

  private ExtendedAccessListLine(Builder builder) {
    _action = requireNonNull(builder._action);
    _dstAddressSpecifier = builder._dstAddressSpecifier;
    _name = builder._name;
    _serviceSpecifier = builder._serviceSpecifier;
    _srcAddressSpecifier = builder._srcAddressSpecifier;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public ExtendedAccessListAddressSpecifier getDestinationAddressSpecifier() {
    return _dstAddressSpecifier;
  }

  public String getName() {
    return _name;
  }

  public ExtendedAccessListServiceSpecifier getServiceSpecifier() {
    return _serviceSpecifier;
  }

  public ExtendedAccessListAddressSpecifier getSourceAddressSpecifier() {
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
