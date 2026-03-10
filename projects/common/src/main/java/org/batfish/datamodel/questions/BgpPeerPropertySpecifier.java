package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.SpecifierFactories;

/**
 * Enables specification of a set of BGP peer properties.
 *
 * <p>Example specifiers:
 *
 * <ul>
 *   <li>local_as —&gt; gets the peer's local AS
 *   <li>.*policy -&gt; gets all properties that end with 'policy'
 * </ul>
 */
@ParametersAreNonnullByDefault
public class BgpPeerPropertySpecifier extends PropertySpecifier {

  public static final String CONFEDERATION = "Confederation";
  public static final String DESCRIPTION = "Description";
  public static final String LOCAL_AS = "Local_AS";
  public static final String LOCAL_IP = "Local_IP";
  public static final String IS_PASSIVE = "Is_Passive";
  public static final String REMOTE_AS = "Remote_AS";
  public static final String ROUTE_REFLECTOR_CLIENT = "Route_Reflector_Client";
  public static final String CLUSTER_ID = "Cluster_ID";
  public static final String PEER_GROUP = "Peer_Group";
  public static final String IMPORT_POLICY = "Import_Policy";
  public static final String EXPORT_POLICY = "Export_Policy";
  public static final String SEND_COMMUNITY = "Send_Community";

  /**
   * Some properties are reported by address family, and some peers don't have all address families
   * present.
   *
   * <p>Prefer reporting properties for IPv4 Unicast address family, but fall back if that is not
   * present.
   */
  private static @Nonnull Optional<AddressFamily> getReportingAddressFamily(BgpPeerConfig c) {
    if (c.getIpv4UnicastAddressFamily() != null) {
      return Optional.of(c.getIpv4UnicastAddressFamily());
    } else if (c.getEvpnAddressFamily() != null) {
      return Optional.of(c.getEvpnAddressFamily());
    }
    return Optional.empty();
  }

  private static final Map<String, PropertyDescriptor<BgpPeerConfig>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<BgpPeerConfig>>()
          .put(
              LOCAL_AS,
              new PropertyDescriptor<>(BgpPeerConfig::getLocalAs, Schema.LONG, "Local AS number"))
          .put(
              LOCAL_IP,
              new PropertyDescriptor<>(
                  BgpPeerPropertySpecifier::getLocalIp,
                  Schema.IP,
                  "Local IPv4 address (null for BGP unnumbered peers)"))
          .put(
              IS_PASSIVE,
              new PropertyDescriptor<>(
                  BgpPeerPropertySpecifier::getIsPassive,
                  Schema.BOOLEAN,
                  "Whether this peer is passive"))
          .put(
              REMOTE_AS,
              new PropertyDescriptor<>(
                  BgpPeerConfig::getRemoteAsns,
                  Schema.STRING,
                  "Remote AS numbers with which this peer may establish a session"))
          .put(
              ROUTE_REFLECTOR_CLIENT,
              new PropertyDescriptor<>(
                  c ->
                      getReportingAddressFamily(c)
                          .map(AddressFamily::getRouteReflectorClient)
                          .orElse(false),
                  Schema.BOOLEAN,
                  "Whether this peer is a route reflector client"))
          .put(
              CLUSTER_ID,
              new PropertyDescriptor<>(
                  BgpPeerPropertySpecifier::getClusterId,
                  Schema.IP,
                  "Cluster ID of this peer (null for peers that are not route reflector clients)"))
          .put(
              PEER_GROUP,
              new PropertyDescriptor<>(
                  BgpPeerConfig::getGroup,
                  Schema.STRING,
                  "Name of the BGP peer group to which this peer belongs"))
          .put(
              IMPORT_POLICY,
              new PropertyDescriptor<>(
                  c ->
                      getReportingAddressFamily(c)
                          .map(AddressFamily::getImportPolicySources)
                          .orElse(ImmutableSortedSet.of()),
                  Schema.set(Schema.STRING),
                  "Names of import policies to be applied to routes received by this peer"))
          .put(
              EXPORT_POLICY,
              new PropertyDescriptor<>(
                  c ->
                      getReportingAddressFamily(c)
                          .map(AddressFamily::getExportPolicySources)
                          .orElse(ImmutableSortedSet.of()),
                  Schema.set(Schema.STRING),
                  "Names of export policies to be applied to routes exported by this peer"))
          .put(
              SEND_COMMUNITY,
              new PropertyDescriptor<>(
                  c ->
                      getReportingAddressFamily(c)
                          .map(AddressFamily::getAddressFamilyCapabilities)
                          .map(AddressFamilyCapabilities::getSendCommunity)
                          .orElse(false),
                  Schema.BOOLEAN,
                  "Whether this peer propagates communities"))
          .put(
              CONFEDERATION,
              new PropertyDescriptor<>(
                  BgpPeerConfig::getConfederationAsn, Schema.LONG, "Confederation AS number"))
          .put(
              DESCRIPTION,
              new PropertyDescriptor<>(
                  BgpPeerConfig::getDescription, Schema.STRING, "Configured peer description"))
          .build();

  /** Returns the property descriptor for {@code property} */
  public static PropertyDescriptor<BgpPeerConfig> getPropertyDescriptor(String property) {
    checkArgument(JAVA_MAP.containsKey(property), "Property " + property + " does not exist");
    return JAVA_MAP.get(property);
  }

  /** A {@link BgpPeerPropertySpecifier} that matches all BGP properties. */
  public static final BgpPeerPropertySpecifier ALL =
      new BgpPeerPropertySpecifier(JAVA_MAP.keySet());

  private final List<String> _properties;

  /**
   * Create a bgp peer property specifier from provided expression. If the expression is null or
   * empty, a specifier with all properties is returned.
   */
  public static BgpPeerPropertySpecifier create(@Nullable String expression) {
    return new BgpPeerPropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression,
                Grammar.BGP_PEER_PROPERTY_SPECIFIER,
                new ConstantEnumSetSpecifier<>(JAVA_MAP.keySet()))
            .resolve());
  }

  /** Returns a specifier that maps to all properties in {@code properties} */
  public BgpPeerPropertySpecifier(Set<String> properties) {
    Set<String> diffSet = Sets.difference(properties, JAVA_MAP.keySet());
    checkArgument(
        diffSet.isEmpty(),
        "Invalid properties supplied: %s. Valid properties are %s",
        diffSet,
        JAVA_MAP.keySet());
    _properties = properties.stream().sorted().collect(ImmutableList.toImmutableList());
  }

  /** Returns cluster ID of this peer */
  private static Object getClusterId(BgpPeerConfig peer) {
    return peer.getAllAddressFamilies().stream().noneMatch(AddressFamily::getRouteReflectorClient)
            || peer.getClusterId() == null
        ? null
        : Ip.create(peer.getClusterId());
  }

  @Override
  public List<String> getMatchingProperties() {
    return _properties;
  }

  private static Ip getLocalIp(@Nonnull BgpPeerConfig peer) {
    // Do not expose local IP of unnumbered peers
    return peer instanceof BgpUnnumberedPeerConfig ? null : peer.getLocalIp();
  }

  @VisibleForTesting
  static boolean getIsPassive(@Nonnull BgpPeerConfig peer) {
    if (peer instanceof BgpActivePeerConfig || peer instanceof BgpUnnumberedPeerConfig) {
      return false;
    }
    if (peer instanceof BgpPassivePeerConfig) {
      return true;
    }
    throw new IllegalArgumentException(String.format("Unrecognized peer type: %s", peer));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BgpPeerPropertySpecifier)) {
      return false;
    }
    return _properties.equals(((BgpPeerPropertySpecifier) o)._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_properties);
  }
}
