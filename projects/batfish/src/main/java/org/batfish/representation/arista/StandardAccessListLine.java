package org.batfish.representation.arista;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;

@ParametersAreNonnullByDefault
public class StandardAccessListLine implements Serializable {

  private final @Nonnull LineAction _action;
  private final @Nonnull String _name;
  private final @Nonnull StandardAccessListServiceSpecifier _serviceSpecifier;
  private final @Nonnull AccessListAddressSpecifier _srcAddressSpecifier;

  public StandardAccessListLine(
      LineAction action,
      String name,
      StandardAccessListServiceSpecifier serviceSpecifier,
      AccessListAddressSpecifier srcAddressSpecifier) {
    _action = action;
    _name = name;
    _serviceSpecifier = serviceSpecifier;
    _srcAddressSpecifier = srcAddressSpecifier;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull StandardAccessListServiceSpecifier getServiceSpecifier() {
    return _serviceSpecifier;
  }

  public @Nonnull AccessListAddressSpecifier getSrcAddressSpecifier() {
    return _srcAddressSpecifier;
  }

  public @Nonnull ExtendedAccessListLine toExtendedAccessListLine() {
    return ExtendedAccessListLine.builder()
        .setAction(_action)
        .setDstAddressSpecifier(new WildcardAddressSpecifier(IpWildcard.ANY))
        .setName(_name)
        .setServiceSpecifier(_serviceSpecifier)
        .setSrcAddressSpecifier(_srcAddressSpecifier)
        .build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("action", _action)
        .add("name", _name)
        .add("serviceSpecifier", _serviceSpecifier)
        .add("srcAddressSpecifier", _srcAddressSpecifier)
        .toString();
  }
}
