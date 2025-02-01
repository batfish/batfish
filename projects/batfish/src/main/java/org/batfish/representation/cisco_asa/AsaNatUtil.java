package org.batfish.representation.cisco_asa;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.representation.cisco_asa.AsaNat.ANY_INTERFACE;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.cisco_asa.AsaNat.Section;

/** Utility methods related to {@link AsaNat}. */
@ParametersAreNonnullByDefault
final class AsaNatUtil {
  private AsaNatUtil() {}

  static @Nullable Transformation.Builder dynamicTransformation(
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
    if (!insideInterface.equals(ANY_INTERFACE)) {
      matchExpr = and(matchExpr, matchSrcInterface(insideInterface));
    }
    return Transformation.when(matchExpr)
        .apply(assignSourceIp(mappedSourceObj.getStart(), mappedSourceObj.getEnd()));
  }

  private static Prefix getEqualLengthPrefix(WildcardAddressSpecifier specifier, Prefix prefix) {
    return Prefix.create(specifier.getIpWildcard().getIp(), prefix.getPrefixLength());
  }

  private static @Nullable Prefix getNetworkObjectPrefix(
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

  /**
   * Determine if an object NAT is an identity NAT. Identity NATs are NATs which do not transform
   * packets, but can be used in conjunction with other NATs. NAT divert will not occur for object
   * NATs unless there is an "actual" transformation, i.e. not an identity transformation.
   *
   * @param nat An object NAT
   * @param networkObjects Mapping of network object names to {@link NetworkObject}s
   * @param w A logger for warnings about unsupported configuration
   * @return True if the object NAT is an identity NAT, or false if it is not. If the answer could
   *     not be determined because the NAT is not supported or invalid, returns null.
   */
  static @Nullable Boolean isIdentityObjectNat(
      AsaNat nat, Map<String, NetworkObject> networkObjects, Warnings w) {
    checkArgument(nat.getSection().equals(Section.OBJECT), "Only supports object NATs.");

    if (nat.getDynamic()) {
      return false;
    }
    Prefix realPrefix =
        getNetworkObjectPrefix(
            (NetworkObjectAddressSpecifier) nat.getRealSource(), networkObjects, w);
    if (realPrefix == null) {
      return null;
    }
    AccessListAddressSpecifier mappedSource = nat.getMappedSource();
    if (mappedSource == null) {
      // undefined reference to a network object or network object group, already warned
      return null;
    }
    if (mappedSource instanceof NetworkObjectGroupAddressSpecifier) {
      // Not supported, will warn about this when creating transformations
      return null;
    }
    if (mappedSource instanceof NetworkObjectAddressSpecifier) {
      return realPrefix.equals(
          getNetworkObjectPrefix((NetworkObjectAddressSpecifier) mappedSource, networkObjects, w));
    }
    if (mappedSource instanceof WildcardAddressSpecifier) {
      // Specified as 'any'
      if (mappedSource.toIpSpace().equals(IpWildcard.ANY.toIpSpace())) {
        return realPrefix.equals(Prefix.ZERO);
      }
      // Specified as inline IP, so only compare start IP
      return realPrefix
          .getStartIp()
          .equals(((WildcardAddressSpecifier) mappedSource).getIpWildcard().getIp());
    }
    throw new BatfishException("Unexpected NetworkObject type");
  }

  private static AclLineMatchExpr matchField(Prefix prefix, IpField field) {
    return switch (field) {
      case DESTINATION -> matchDst(prefix);
      case SOURCE -> matchSrc(prefix);
    };
  }

  static Optional<Transformation.Builder> secondTransformation(
      AccessListAddressSpecifier shiftDestination,
      AccessListAddressSpecifier matchDestination,
      Transformation first,
      Map<String, NetworkObject> networkObjects,
      IpField field,
      Warnings w) {

    Transformation.Builder secondBuilder =
        staticTransformation(
            matchDestination, shiftDestination, ANY_INTERFACE, networkObjects, field, w);
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
    return switch (field) {
      case DESTINATION -> TransformationStep.shiftDestinationIp(subnet);
      case SOURCE -> TransformationStep.shiftSourceIp(subnet);
    };
  }

  static @Nullable Transformation.Builder staticTransformation(
      AccessListAddressSpecifier matchAddress,
      AccessListAddressSpecifier shiftAddress,
      String insideInterface,
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
          "Matching 'any' and shifting to an object or object group, or vice versa, is not"
              + " supported.");
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
        matchPrefix =
            shiftPrefix == null
                ? null
                : getEqualLengthPrefix((WildcardAddressSpecifier) matchAddress, shiftPrefix);
      } else if (shiftAddress instanceof WildcardAddressSpecifier
          && matchAddress instanceof NetworkObjectAddressSpecifier) {
        matchPrefix =
            getNetworkObjectPrefix((NetworkObjectAddressSpecifier) matchAddress, networkObjects, w);
        shiftPrefix =
            matchPrefix == null
                ? null
                : getEqualLengthPrefix((WildcardAddressSpecifier) shiftAddress, matchPrefix);
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
            "Matching and shifting objects do not have prefixes of equal length, which is not"
                + " supported.");
        return null;
      }
    }
    AclLineMatchExpr matchExpr = matchField(matchPrefix, field);
    if (!insideInterface.equals(ANY_INTERFACE)) {
      matchExpr = and(matchExpr, matchSrcInterface(insideInterface));
    }
    return Transformation.when(matchExpr).apply(shiftIp(field, shiftPrefix));
  }

  /**
   * Completes the conversion of Cisco ASA-specific NATs to a single {@link Transformation}.
   *
   * @param convertedNats A list of partially built {@link Transformation}s.
   * @return A single {@link Transformation} or null if empty
   */
  static @Nullable Transformation toTransformationChain(
      List<Optional<Transformation.Builder>> convertedNats) {

    // Start at the end of the chain and go backwards.
    Transformation previous = null;
    for (Optional<Transformation.Builder> t : Lists.reverse(convertedNats)) {
      if (t.isPresent()) {
        previous = t.get().setOrElse(previous).build();
      }
    }
    return previous;
  }
}
