package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
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
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.EnumSetSpecifier;
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

  public static final Map<String, PropertyDescriptor<BgpPeerConfig>> JAVA_MAP =
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
                  BgpPeerConfig::getRouteReflectorClient,
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
                  BgpPeerConfig::getImportPolicySources,
                  Schema.set(Schema.STRING),
                  "Names of import policies to be applied to routes received by this peer"))
          .put(
              EXPORT_POLICY,
              new PropertyDescriptor<>(
                  BgpPeerConfig::getExportPolicySources,
                  Schema.set(Schema.STRING),
                  "Names of export policies to be applied to routes exported by this peer"))
          .put(
              SEND_COMMUNITY,
              new PropertyDescriptor<>(
                  BgpPeerConfig::getSendCommunity,
                  Schema.BOOLEAN,
                  "Whether this peer propagates communities"))
          .build();

  /** A {@link BgpPeerPropertySpecifier} that matches all BGP properties. */
  public static final BgpPeerPropertySpecifier ALL = new BgpPeerPropertySpecifier(".*");

  private final String _expression;

  private final EnumSetSpecifier<String> _enumSetSpecifier;

  @JsonCreator
  private static BgpPeerPropertySpecifier create(@Nullable String expression) {
    return new BgpPeerPropertySpecifier(expression);
  }

  public BgpPeerPropertySpecifier(@Nullable String expression) {
    this(
        expression,
        SpecifierFactories.getEnumSetSpecifierOrDefault(
            expression, JAVA_MAP.keySet(), new ConstantEnumSetSpecifier<>(JAVA_MAP.keySet())));
  }

  /** Returns a specifier that maps to all properties in {@code properties} */
  public BgpPeerPropertySpecifier(Set<String> properties) {
    this(null, new ConstantEnumSetSpecifier<>(properties));
    Set<String> diffSet = Sets.difference(properties, JAVA_MAP.keySet());
    checkArgument(
        diffSet.isEmpty(), "Invalid properties supplied to the property specifier: %s", diffSet);
  }

  private BgpPeerPropertySpecifier(
      @Nullable String expression, EnumSetSpecifier<String> enumSetSpecifier) {
    _expression = expression;
    _enumSetSpecifier = enumSetSpecifier;
  }

  /** Returns cluster ID of this peer */
  private static Object getClusterId(BgpPeerConfig peer) {
    return !peer.getRouteReflectorClient() || peer.getClusterId() == null
        ? null
        : Ip.create(peer.getClusterId());
  }

  @Override
  public List<String> getMatchingProperties() {
    return _enumSetSpecifier.resolve().stream().sorted().collect(ImmutableList.toImmutableList());
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
  @JsonValue
  @Nullable
  public String toString() {
    return _expression;
  }
}
