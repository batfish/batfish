package org.batfish.representation.cisco;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import com.google.common.collect.Iterables;
import java.util.Map;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/** Utility methods related to {@link CiscoAsaNat}. */
@ParametersAreNonnullByDefault
final class CiscoAsaNatUtil {
  private CiscoAsaNatUtil() {}

  static Transformation.Builder dynamicTransformation(
      AccessListAddressSpecifier realSource,
      AccessListAddressSpecifier mappedSource,
      String insideInterface,
      Map<String, NetworkObject> networkObjects) {

    boolean anyMapped = mappedSource.toIpSpace().equals(IpWildcard.ANY.toIpSpace());
    if (anyMapped) {
      // Invalid
      return null;
    }
    // realSource is expected to be a wildcard, network object, or group
    AclLineMatchExpr matchExpr = matchSrc(realSource.toIpSpace());

    if (!(mappedSource instanceof NetworkObjectAddressSpecifier)) {
      // Network object groups not supported for mappedSource
      return null;
    }

    NetworkObject mappedSourceObj =
        networkObjects.get(((NetworkObjectAddressSpecifier) mappedSource).getName());
    if (mappedSourceObj.getRangeStart() == null) {
      // Range is required for NAT. Hosts are for PAT.
      return null;
    }
    if (insideInterface != null) {
      /*
       * Assuming for now that this transformation will be placed on the outside interface. If that
       * is not true and the transformation is placed on the inside interface, this conjunction can
       * be removed.
       */
      matchExpr = and(matchExpr, matchSrcInterface(insideInterface));
    }
    return Transformation.when(matchExpr)
        .apply(assignSourceIp(mappedSourceObj.getRangeStart(), mappedSourceObj.getRangeEnd()));
  }

  private static MatchHeaderSpace matchField(Prefix prefix, IpField field) {
    switch (field) {
      case DESTINATION:
        return new MatchHeaderSpace(HeaderSpace.builder().setDstIps(prefix.toIpSpace()).build());
      case SOURCE:
        return new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(prefix.toIpSpace()).build());
      default:
        throw new BatfishException("Invalid field");
    }
  }

  static Optional<Transformation.Builder> secondTransformation(
      AccessListAddressSpecifier shiftDestination,
      AccessListAddressSpecifier matchDestination,
      Transformation first,
      Map<String, NetworkObject> networkObjects,
      IpField field) {

    if (shiftDestination == null || matchDestination == null) {
      // Invalid reference or not supported
      return Optional.empty();
    }

    Transformation.Builder secondBuilder =
        staticTransformation(matchDestination, shiftDestination, null, networkObjects, field);
    if (secondBuilder == null) {
      return Optional.empty();
    }
    Transformation second = secondBuilder.build();

    return Optional.of(
        Transformation.when(and(first.getGuard(), second.getGuard()))
            .apply(
                Iterables.concat(first.getTransformationSteps(), second.getTransformationSteps())));
  }

  private static ShiftIpAddressIntoSubnet shiftIp(IpField field, Prefix subnet) {
    switch (field) {
      case DESTINATION:
        return TransformationStep.shiftDestinationIp(subnet);
      case SOURCE:
        return TransformationStep.shiftSourceIp(subnet);
      default:
        throw new BatfishException("Unsupported field");
    }
  }

  static Transformation.Builder staticTransformation(
      AccessListAddressSpecifier matchAddress,
      AccessListAddressSpecifier shiftAddress,
      String insideInterface,
      Map<String, NetworkObject> networkObjects,
      IpField field) {

    if (matchAddress == null || shiftAddress == null) {
      // Invalid reference or unsupported
      return null;
    }

    boolean anyMatch = matchAddress.toIpSpace().equals(IpWildcard.ANY.toIpSpace());
    boolean anyShift = shiftAddress.toIpSpace().equals(IpWildcard.ANY.toIpSpace());
    Prefix matchPrefix;
    Prefix shiftPrefix;
    if (anyMatch && anyShift) {
      // Identity NAT matching all traffic
      matchPrefix = Prefix.ZERO;
      shiftPrefix = Prefix.ZERO;
    } else if (anyMatch ^ anyShift) {
      // these uses might result in unpredictable behavior.
      // https://www.cisco.com/c/en/us/td/docs/security/asa/asa-command-reference/I-R/cmdref2/n.html
      return null;
    } else {
      // both matchAddress and shiftAddress are specified and are objects or object groups
      if (!(matchAddress instanceof NetworkObjectAddressSpecifier)
          && !(shiftAddress instanceof NetworkObjectAddressSpecifier)) {
        // Network object groups not supported
        return null;
      }
      NetworkObject matchObject =
          networkObjects.get(((NetworkObjectAddressSpecifier) matchAddress).getName());
      NetworkObject shiftObject =
          networkObjects.get(((NetworkObjectAddressSpecifier) shiftAddress).getName());
      if (matchObject == null || shiftObject == null) {
        // Invalid reference
        return null;
      }

      // matchAddress object is translated into a single prefix for matching
      if (matchObject.getSubnet() != null) {
        matchPrefix = matchObject.getSubnet();
      } else if (matchObject.getHost() != null) {
        matchPrefix = Prefix.create(matchObject.getHost(), Prefix.MAX_PREFIX_LENGTH);
      } else {
        // Object (groups) support hosts, ranges, and subnets. Ranges are not supported.
        return null;
      }

      // shiftAddress object is translated into a single prefix for translation
      if (shiftObject.getSubnet() != null) {
        shiftPrefix = shiftObject.getSubnet();
      } else if (shiftObject.getHost() != null) {
        shiftPrefix = Prefix.create(shiftObject.getHost(), Prefix.MAX_PREFIX_LENGTH);
      } else {
        // Object (groups) support hosts, ranges, and subnets. Ranges are not supported.
        return null;
      }

      /*
       * ASA allows length mismatch for NAT specified via network objects. Only the mappings between
       * the lowest matching addresses and the lowest shift addresses are bidirectional. Other flows
       * are possible: either unidirectional or can only be initiated in one direction.
       * See https://www.cisco.com/c/en/us/td/docs/security/asa/asa910/configuration/firewall/asa-910-firewall-config/nat-basics.html#ID-2090-00000869
       */
      int maxLength = Math.max(shiftPrefix.getPrefixLength(), matchPrefix.getPrefixLength());
      shiftPrefix = Prefix.create(shiftPrefix.getStartIp(), maxLength);
      matchPrefix = Prefix.create(matchPrefix.getStartIp(), maxLength);
    }
    AclLineMatchExpr matchExpr = matchField(matchPrefix, field);
    if (insideInterface != null) {
      /*
       * Assuming for now that this transformation will be placed on the outside interface. If that
       * is not true and the transformation is placed on the inside interface, this conjunction can
       * be removed.
       */
      matchExpr = and(matchExpr, matchSrcInterface(insideInterface));
    }
    return Transformation.when(matchExpr).apply(shiftIp(field, shiftPrefix));
  }
}
