package org.batfish.representation.cisco;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;

public class StandardAccessListLine implements Serializable {

  private final LineAction _action;

  private final String _name;

  private final StandardAccessListServiceSpecifier _serviceSpecifier;

  private final AccessListAddressSpecifier _srcAddressSpecifier;

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

  public LineAction getAction() {
    return _action;
  }

  public String getName() {
    return _name;
  }

  public StandardAccessListServiceSpecifier getServiceSpecifier() {
    return _serviceSpecifier;
  }

  public AccessListAddressSpecifier getSrcAddressSpecifier() {
    return _srcAddressSpecifier;
  }

  public ExtendedAccessListLine toExtendedAccessListLine() {
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
