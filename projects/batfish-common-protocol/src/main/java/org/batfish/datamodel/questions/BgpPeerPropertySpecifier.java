package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.answers.SelfDescribingObject;

/**
 * Enables specification a set of BGP peer properties.
 *
 * <p>Example specifiers:
 *
 * <ul>
 *   <li>multipath-ebgp —&gt; gets the process's corresponding value
 *   <li>multipath-.* --&gt; gets all properties that start with 'max-metric-'
 * </ul>
 */
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
          .put(LOCAL_AS, new PropertyDescriptor<>(BgpPeerConfig::getLocalAs, Schema.LONG))
          .put(LOCAL_IP, new PropertyDescriptor<>(BgpPeerConfig::getLocalIp, Schema.IP))
          .put(IS_PASSIVE, new PropertyDescriptor<>((peer) -> getIsPassive(peer), Schema.BOOLEAN))
          .put(
              REMOTE_AS,
              new PropertyDescriptor<>((peer) -> getRemoteAs(peer), Schema.SELF_DESCRIBING))
          .put(
              ROUTE_REFLECTOR_CLIENT,
              new PropertyDescriptor<>(BgpPeerConfig::getRouteReflectorClient, Schema.BOOLEAN))
          .put(CLUSTER_ID, new PropertyDescriptor<>((peer) -> getClusterId(peer), Schema.IP))
          .put(PEER_GROUP, new PropertyDescriptor<>(BgpPeerConfig::getGroup, Schema.STRING))
          .put(
              IMPORT_POLICY,
              new PropertyDescriptor<>(
                  BgpPeerConfig::getImportPolicySources, Schema.set(Schema.STRING)))
          .put(
              EXPORT_POLICY,
              new PropertyDescriptor<>(
                  BgpPeerConfig::getExportPolicySources, Schema.set(Schema.STRING)))
          .put(
              SEND_COMMUNITY,
              new PropertyDescriptor<>(BgpPeerConfig::getSendCommunity, Schema.BOOLEAN))
          .build();

  /** A {@link BgpPeerPropertySpecifier} that matches all BGP properties. */
  public static final BgpPeerPropertySpecifier ALL = new BgpPeerPropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public BgpPeerPropertySpecifier(@Nullable String expression) {
    _expression = firstNonNull(expression, ".*");
    _pattern = Pattern.compile(_expression.trim().toLowerCase()); // canonicalize
  }

  /** Returns a specifier that maps to all properties in {@code properties} */
  public BgpPeerPropertySpecifier(Collection<String> properties) {
    // quote and join
    _expression =
        properties.stream().map(String::trim).map(Pattern::quote).collect(Collectors.joining("|"));
    _pattern = Pattern.compile(_expression, Pattern.CASE_INSENSITIVE);
  }

  /**
   * Returns a list of suggestions based on the query, based on {@link
   * PropertySpecifier#baseAutoComplete}.
   */
  public static List<AutocompleteSuggestion> autoComplete(String query) {
    return PropertySpecifier.baseAutoComplete(query, JAVA_MAP.keySet());
  }

  /** Returns cluster ID of this peer */
  private static Object getClusterId(BgpPeerConfig peer) {
    return !peer.getRouteReflectorClient() || peer.getClusterId() == null
        ? null
        : new Ip(peer.getClusterId());
  }

  @Override
  public Set<String> getMatchingProperties() {
    return JAVA_MAP
        .keySet()
        .stream()
        .filter(prop -> _pattern.matcher(prop.toLowerCase()).matches())
        .collect(Collectors.toSet());
  }

  @VisibleForTesting
  static boolean getIsPassive(@Nonnull BgpPeerConfig peer) {
    if (peer instanceof BgpActivePeerConfig) {
      return false;
    }
    if (peer instanceof BgpPassivePeerConfig) {
      return true;
    }
    throw new IllegalArgumentException(
        String.format("Peer is neither Active nor Passive: %s", peer));
  }

  @VisibleForTesting
  static SelfDescribingObject getRemoteAs(@Nonnull BgpPeerConfig peer) {
    if (peer instanceof BgpActivePeerConfig) {
      return new SelfDescribingObject(Schema.LONG, ((BgpActivePeerConfig) peer).getRemoteAs());
    }
    if (peer instanceof BgpPassivePeerConfig) {
      return new SelfDescribingObject(
          Schema.list(Schema.LONG), ((BgpPassivePeerConfig) peer).getRemoteAs());
    }
    throw new IllegalArgumentException(
        String.format("Peer is neither Active nor Passive: %s", peer));
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
