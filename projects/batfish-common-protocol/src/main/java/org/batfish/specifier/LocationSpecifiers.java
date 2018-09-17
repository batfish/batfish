package org.batfish.specifier;

import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.InterfacesSpecifier;
import org.batfish.datamodel.questions.NodesSpecifier;

public final class LocationSpecifiers {
  private LocationSpecifiers() {}

  public static final LocationSpecifier ALL_LOCATIONS =
      new UnionLocationSpecifier(
          AllInterfacesLocationSpecifier.INSTANCE, AllInterfaceLinksLocationSpecifier.INSTANCE);

  public static LocationSpecifier difference(
      @Nonnull LocationSpecifier locsIn, @Nonnull LocationSpecifier locsOut) {
    if (locsIn == NullLocationSpecifier.INSTANCE) {
      return NullLocationSpecifier.INSTANCE;
    }

    if (locsOut == NullLocationSpecifier.INSTANCE) {
      return locsIn;
    }

    return new DifferenceLocationSpecifier(locsIn, locsOut);
  }

  public static LocationSpecifier from(@Nonnull InterfacesSpecifier ingressInterfaces) {
    switch (ingressInterfaces.getType()) {
      case DESC:
        return new DescriptionRegexInterfaceLocationSpecifier(ingressInterfaces.getRegex());
      case NAME:
        return new NameRegexInterfaceLocationSpecifier(ingressInterfaces.getRegex());
      case VRF:
        return new VrfNameRegexInterfaceLocationSpecifier(ingressInterfaces.getRegex());
      default:
        throw new BatfishException(
            "Unexcepted InterfacesSpecifier type: " + ingressInterfaces.getType());
    }
  }

  public static LocationSpecifier from(@Nonnull NodesSpecifier ingressNodes) {
    switch (ingressNodes.getType()) {
      case NAME:
        return new NodeNameRegexInterfaceLocationSpecifier(ingressNodes.getRegex());
      case ROLE:
        return new NodeRoleRegexInterfaceLocationSpecifier(
            ingressNodes.getRoleDimension(), ingressNodes.getRegex());
      default:
        throw new BatfishException("Unexpected NodesSpecifier type: " + ingressNodes.getType());
    }
  }
}
