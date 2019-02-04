package org.batfish.representation.cisco;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;

import com.google.common.collect.Iterables;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
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
      Map<String, NetworkObject> networkObjects,
      Warnings w) {

    boolean anyMapped = mappedSource.toIpSpace().equals(IpWildcard.ANY.toIpSpace());
    if (anyMapped) {
      // Invalid
      w.redFlag("Cannot assign from 'any' IP address.");
      return null;
    }
    // realSource is expected to be a wildcard, network object, or group
    AclLineMatchExpr matchExpr = matchSrc(realSource.toIpSpace());

    if (!(mappedSource instanceof NetworkObjectAddressSpecifier)) {
      /*
       * Network object groups not supported for mappedSource
       * A valid network object group here would consist of one or more ranges and hosts.
       * The correct behavior is to treat the ranges and hosts as a sequence of address pools and
       * PAT fallback addresses.
       */
      w.redFlag("No support for assigning addresses from network object groups");
      return null;
    }

    NetworkObject mappedSourceObj =
        networkObjects.get(((NetworkObjectAddressSpecifier) mappedSource).getName());
    if (mappedSourceObj instanceof HostNetworkObject) {
      w.redFlag("PAT is not supported.");
      return null;
    }
    if (!(mappedSourceObj instanceof RangeNetworkObject)) {
      // Invalid network object, must be a host or a range
      w.redFlag("Invalid network object for assigning addresses " + mappedSourceObj.getName());
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
        .apply(assignSourceIp(mappedSourceObj.getStart(), mappedSourceObj.getEnd()));
  }

  private static Prefix getEqualLengthPrefix(WildcardAddressSpecifier specifier, Prefix prefix) {
    return Prefix.create(specifier.getIpWildcard().getIp(), prefix.getPrefixLength());
  }

  private static Prefix getNetworkObjectPrefix(
      NetworkObjectAddressSpecifier specifier,
      Map<String, NetworkObject> networkObjects,
      Warnings w) {
    NetworkObject object = networkObjects.get(specifier.getName());
    if (object == null) {
      // Previously warned about undefined reference
      return null;
    }

    if (object instanceof HostNetworkObject) {
      return ((HostNetworkObject) object).getPrefix();
    }
    if (object instanceof SubnetNetworkObject) {
      return ((SubnetNetworkObject) object).getPrefix();
    }
    if (object instanceof FqdnNetworkObject) {
      // Previously warned that these are not supported
      return null;
    }
    if (object instanceof RangeNetworkObject) {
      // These are supported for dynamic NAT but not static NAT
      w.redFlag("Ranges are not supported for static NAT");
      return null;
    }
    throw new BatfishException("Unexpected network object type");
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
      @Nullable AccessListAddressSpecifier shiftDestination,
      @Nullable AccessListAddressSpecifier matchDestination,
      Transformation first,
      Map<String, NetworkObject> networkObjects,
      IpField field,
      Warnings w) {

    if (shiftDestination == null || matchDestination == null) {
      // Invalid reference or not supported
      w.redFlag("Invalid match or shift destination.");
      return Optional.empty();
    }

    Transformation.Builder secondBuilder =
        staticTransformation(matchDestination, shiftDestination, null, networkObjects, field, w);
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
      @Nullable String insideInterface,
      Map<String, NetworkObject> networkObjects,
      IpField field,
      Warnings w) {

    boolean anyMatch = matchAddress.toIpSpace().equals(IpWildcard.ANY.toIpSpace());
    boolean anyShift = shiftAddress.toIpSpace().equals(IpWildcard.ANY.toIpSpace());
    Prefix matchPrefix;
    Prefix shiftPrefix;
    /*
     * There are valid cases which are not supported here
     * 1) Ranges do not map to prefixes but are valid. There is currently no support for
     *    matching a range or shifting into it.
     * 2) Prefixes of unequal length are valid but not always recommended.
     *    See https://www.cisco.com/c/en/us/td/docs/security/asa/asa910/configuration/firewall/asa-910-firewall-config/nat-basics.html#ID-2090-00000869
     * 3) Network object groups are not supported. A simple network object group might involve
     *    matching and shifting to and from prefixes, but if there are ranges in the object group
     *    or any of the prefixes in the matching group do not align with a prefix in the shift
     *    object group, then range matching/shifting is required.
     */
    if (anyMatch && anyShift) {
      // Identity NAT matching all traffic
      matchPrefix = Prefix.ZERO;
      shiftPrefix = Prefix.ZERO;
    } else if (anyMatch ^ anyShift) {
      // these uses might result in unpredictable behavior.
      // https://www.cisco.com/c/en/us/td/docs/security/asa/asa-command-reference/I-R/cmdref2/n.html
      w.redFlag(
          "Matching 'any' and shifting to an object or object group, or vice versa, is not supported.");
      return null;
    } else {
      // both matchAddress and shiftAddress are specified and are objects, object groups, or an
      // inline IP.
      if (matchAddress instanceof NetworkObjectGroupAddressSpecifier
          || shiftAddress instanceof NetworkObjectGroupAddressSpecifier) {
        // Network object groups not supported
        w.redFlag("Network object groups not supported for static transformations.");
        return null;
      }
      if (matchAddress instanceof WildcardAddressSpecifier
          && shiftAddress instanceof NetworkObjectAddressSpecifier) {
        // If match or shift address was specified as an inline IP, the prefix length was not known
        // at the time it was created. Get the correct prefix length.
        shiftPrefix =
            getNetworkObjectPrefix((NetworkObjectAddressSpecifier) shiftAddress, networkObjects, w);
        matchPrefix = getEqualLengthPrefix((WildcardAddressSpecifier) matchAddress, shiftPrefix);
      } else if (shiftAddress instanceof WildcardAddressSpecifier
          && matchAddress instanceof NetworkObjectAddressSpecifier) {
        matchPrefix =
            getNetworkObjectPrefix((NetworkObjectAddressSpecifier) matchAddress, networkObjects, w);
        shiftPrefix = getEqualLengthPrefix((WildcardAddressSpecifier) shiftAddress, matchPrefix);
      } else if (matchAddress instanceof NetworkObjectAddressSpecifier
          && shiftAddress instanceof NetworkObjectAddressSpecifier) {
        matchPrefix =
            getNetworkObjectPrefix((NetworkObjectAddressSpecifier) matchAddress, networkObjects, w);
        shiftPrefix =
            getNetworkObjectPrefix((NetworkObjectAddressSpecifier) shiftAddress, networkObjects, w);
      } else {
        w.redFlag("Unsupported address specifier.");
        return null;
      }
      if (matchPrefix == null || shiftPrefix == null) {
        // Undefined reference or unsupported network object/group which was previously warned about
        return null;
      }

      if (matchPrefix.getPrefixLength() != shiftPrefix.getPrefixLength()) {
        w.redFlag(
            "Matching and shifting objects do not have prefixes of equal length, which is not supported.");
        return null;
      }
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
