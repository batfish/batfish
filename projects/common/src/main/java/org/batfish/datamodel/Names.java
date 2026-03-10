package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Provides helper methods to auto-generate structure names and to check the validity of names of
 * different types of entities.
 */
@ParametersAreNonnullByDefault
public final class Names {

  /**
   * Enum for different types of names with regexes that describes valid names.
   *
   * <p>If the regex of any of the names here is changed, ensure that corresponding grammar rules in
   * org.batfish.specifier.parboiled.CommonParser are also updated
   */
  public enum Type {
    /**
     * All names inside {@link org.batfish.referencelibrary.ReferenceLibrary} and {@link
     * org.batfish.role.NodeRolesData}.
     */
    REFERENCE_OBJECT(
        "^\\p{ASCII}+$", "be non-empty and contain only (non-extended) ASCII characters"),
    /** Column names in {@link org.batfish.datamodel.table.ColumnMetadata} */
    TABLE_COLUMN(
        "^[a-zA-Z0-9_~][-/\\w\\.:~@]*$",
        "start with alphanumeric, underscore or tilde and only have  [-/.:~@]");

    Type(String regex, String explanation) {
      _regex = regex;
      _explanation = explanation;
    }

    private final String _explanation;

    private final String _regex;

    public String getExplanation() {
      return _explanation;
    }

    public String getRegex() {
      return _regex;
    }
  }

  /** We use double quotes to escape complex names */
  public static final String ESCAPE_CHAR = "\"";

  /**
   * Characters that we deem special and cannot appear in unquoted names. We are currently using the
   * first bunch and setting aside some more for future use.
   *
   * <p>Once we stop supporting now-deprecated regexes, '*' should probably added to the reserved
   * list.
   */
  public static final String SPECIAL_CHARS = " \t,\\&()[]@" + "!#$%^;?<>={}";

  private static final char[] SPECIAL_CHARS_ARRAY = SPECIAL_CHARS.toCharArray();

  @VisibleForTesting
  static final Map<Type, Pattern> VALID_PATTERNS =
      Arrays.stream(Type.values())
          .collect(
              ImmutableMap.toImmutableMap(Function.identity(), o -> Pattern.compile(o.getRegex())));

  private Names() {} // prevent instantiation by default.

  /**
   * Checks if {@code name} is valid for {@code type}.
   *
   * <p>{@code objectDescription} is the user-facing description of the object type.
   *
   * @throws IllegalArgumentException if the name is invalid.
   */
  public static void checkName(String name, String objectDescription, Type type) {
    checkArgument(
        VALID_PATTERNS.get(type).matcher(name).matches(),
        "Invalid %s name '%s'. Valid names must %s.",
        objectDescription,
        name,
        type.getExplanation());
  }

  public static String bgpNeighborStructureName(String neighborName, String vrfName) {
    return String.format("%s (VRF %s)", neighborName, vrfName);
  }

  public static String generatedNegatedTrackMethodId(String trackMethodId) {
    return String.format("~!%s~", trackMethodId);
  }

  public static String generatedBgpCommonExportPolicyName(String vrf) {
    return String.format("~BGP_COMMON_EXPORT_POLICY:%s~", vrf);
  }

  public static String generatedBgpDefaultRouteExportPolicyName() {
    return "~BGP_DEFAULT_ROUTE_PEER_EXPORT_POLICY:IPv4~";
  }

  public static String generatedBgpIndependentNetworkPolicyName(String vrf) {
    return String.format("~BGP_INDEPENDENT_NETWORK_POLICY:%s~", vrf);
  }

  public static String generatedBgpMainRibIndependentNetworkPolicyName(String vrf) {
    return String.format("~BGP_MAIN_RIB_INDEPENDENT_NETWORK_POLICY:%s~", vrf);
  }

  public static String generatedBgpRedistributionPolicyName(String vrf) {
    return String.format("~BGP_REDISTRIBUTION_POLICY:%s~", vrf);
  }

  public static String generatedBgpPeerExportPolicyName(String vrf, String peer) {
    return String.format("~BGP_PEER_EXPORT_POLICY:%s:%s~", vrf, peer);
  }

  public static String generatedBgpPeerImportPolicyName(String vrf, String peer) {
    return String.format("~BGP_PEER_IMPORT_POLICY:%s:%s~", vrf, peer);
  }

  public static String generatedBgpPeerEvpnExportPolicyName(String vrf, String peer) {
    return String.format("~BGP_PEER_EXPORT_POLICY_EVPN:%s:%s~", vrf, peer);
  }

  public static String generatedBgpPeerEvpnImportPolicyName(String vrf, String peer) {
    return String.format("~BGP_PEER_IMPORT_POLICY_EVPN:%s:%s~", vrf, peer);
  }

  public static String generatedEvpnToBgpv4VrfLeakPolicyName(String vrf) {
    return String.format("~EVPN_TO_BGPV4_VRF_LEAK_POLICY:%s~", vrf);
  }

  public static String generatedOspfDefaultRouteGenerationPolicyName(String vrf, String proc) {
    return String.format("~OSPF_DEFAULT_ROUTE_GENERATION_POLICY:%s:%s~", vrf, proc);
  }

  public static String generatedOspfExportPolicyName(String vrf, String proc) {
    return String.format("~OSPF_EXPORT_POLICY:%s:%s~", vrf, proc);
  }

  public static String generatedOspfInboundDistributeListName(
      String vrf, String procName, long areaNum, String ifaceName) {
    return String.format(
        "~OSPF_INBOUND_DISTRIBUTE_LIST:%s:%s:%s:%s~", vrf, procName, areaNum, ifaceName);
  }

  public static String generatedReferenceBook(String hostname, String source) {
    return String.format("%s~on~%s", source, hostname);
  }

  public static String generatedTenantVniInterfaceName(int vni) {
    return String.format("nve~%d", vni);
  }

  /**
   * Return the Batfish canonical name for a filter between zones.
   *
   * <p>This should only be used for filters that are defined by the user but unnamed in the vendor
   * language, rather than filters that are "generated" by Batfish combining multiple user-defined
   * structures.
   */
  public static String zoneToZoneFilter(String fromZone, String toZone) {
    return String.format("zone~%s~to~zone~%s", fromZone, toZone);
  }

  /** Checks if the provided name needs to be escaped per our rules */
  public static boolean nameNeedsEscaping(@Nullable String name) {
    return name != null
        && !name.isEmpty()
        && (name.startsWith(ESCAPE_CHAR)
            || Character.isDigit(name.charAt(0))
            || name.startsWith("/")
            || nameContainsSpecialChar(name));
  }

  private static boolean nameContainsSpecialChar(String name) {
    for (char c : SPECIAL_CHARS_ARRAY) {
      if (name.indexOf(c) >= 0) {
        return true;
      }
    }
    return false;
  }

  /** Returns the escaped named if it needs escaping else return the original */
  public static String escapeNameIfNeeded(@Nullable String name) {
    return nameNeedsEscaping(name) ? ESCAPE_CHAR + name + ESCAPE_CHAR : name;
  }
}
