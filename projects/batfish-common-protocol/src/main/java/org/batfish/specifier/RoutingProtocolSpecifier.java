package org.batfish.specifier;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.questions.PropertySpecifier;

/** A way to specify groups of RoutingProtocols */
@ParametersAreNonnullByDefault
public class RoutingProtocolSpecifier {

  // Hierarchy:

  //  IGP
  //      OSPF
  //          OSPF-INT
  //              OSPF-INTRA
  //              OSPF-INTER
  //          OSPF-EXT
  //              OSPF-EXT1
  //              OSPF-EXT2
  //      ISIS
  //          ISIS-L1
  //          ISIS-L2
  //      EIGRP
  //          EIGRP-INT
  //          EIGRP-EXT
  //      RIP
  //  BGP
  //      EBGP
  //      IBGP
  //  AGGREGATE
  //  STATIC
  //  LOCAL
  //  CONNECTED

  public static final String ALL = "all";

  private static final String AGGREGATE = "aggregate";
  @VisibleForTesting static final String BGP = "bgp";
  private static final String CONNECTED = "connected";
  @VisibleForTesting static final String EBGP = "ebgp";
  private static final String EIGRP = "eigrp";
  private static final String EIGRP_EXT = "eigrp-ext";
  private static final String EIGRP_INT = "eigrp-int";
  @VisibleForTesting static final String IBGP = "ibgp";
  private static final String IGP = "igp";
  private static final String ISIS = "isis";
  private static final String ISIS_L1 = "isis-l1";
  private static final String ISIS_L2 = "isis-l2";
  private static final String LOCAL = "local";
  private static final String OSPF = "ospf";
  private static final String OSPF_EXT = "ospf-ext";
  private static final String OSPF_EXT1 = "ospf-ext1";
  private static final String OSPF_EXT2 = "ospf-ext2";
  private static final String OSPF_INT = "ospf-int";
  private static final String OSPF_INTER = "ospf-inter";
  private static final String OSPF_INTRA = "ospf-intra";
  private static final String RIP = "rip";
  private static final String STATIC = "static";

  private static final RoutingProtocol AGGREGATE_PROTOCOL = RoutingProtocol.AGGREGATE;
  private static final RoutingProtocol CONNECTED_PROTOCOL = RoutingProtocol.CONNECTED;
  private static final RoutingProtocol EBGP_PROTOCOL = RoutingProtocol.BGP;
  private static final RoutingProtocol EIGRP_EXT_PROTOCOL = RoutingProtocol.EIGRP_EX;
  private static final RoutingProtocol EIGRP_INT_PROTOCOL = RoutingProtocol.EIGRP;
  private static final RoutingProtocol IBGP_PROTOCOL = RoutingProtocol.IBGP;
  private static final RoutingProtocol ISIS_L1_PROTOCOL = RoutingProtocol.ISIS_L1;
  private static final RoutingProtocol ISIS_L2_PROTOCOL = RoutingProtocol.ISIS_L2;
  private static final RoutingProtocol LOCAL_PROTOCOL = RoutingProtocol.LOCAL;
  private static final RoutingProtocol OSPF_EXT1_PROTOCOL = RoutingProtocol.OSPF_E1;
  private static final RoutingProtocol OSPF_EXT2_PROTOCOL = RoutingProtocol.OSPF_E2;
  private static final RoutingProtocol OSPF_INTER_PROTOCOL = RoutingProtocol.OSPF_IA;
  private static final RoutingProtocol OSPF_INTRA_PROTOCOL = RoutingProtocol.OSPF;
  private static final RoutingProtocol RIP_PROTOCOL = RoutingProtocol.RIP;
  private static final RoutingProtocol STATIC_PROTOCOL = RoutingProtocol.STATIC;

  private static final Set<RoutingProtocol> ALL_PROTOCOLS =
      ImmutableSet.<RoutingProtocol>builder()
          .addAll(Stream.of(RoutingProtocol.values()).collect(Collectors.toSet()))
          .build();

  private static final Set<RoutingProtocol> OSPF_INT_PROTOCOLS;

  private static final Set<RoutingProtocol> OSPF_EXT_PROTOCOLS;

  private static final Set<RoutingProtocol> OSPF_PROTOCOLS;

  private static final Set<RoutingProtocol> ISIS_PROTOCOLS;

  private static final Set<RoutingProtocol> EIGRP_PROTOCOLS;

  private static final Set<RoutingProtocol> IGP_PROTOCOLS;

  private static final Set<RoutingProtocol> BGP_PROTOCOLS;

  private static final Map<String, Set<RoutingProtocol>> MAP;

  public static final RoutingProtocolSpecifier ALL_PROTOCOLS_SPECIFIER;

  // initialize all protocol groups
  static {
    OSPF_INT_PROTOCOLS = ImmutableSet.of(OSPF_INTRA_PROTOCOL, OSPF_INTER_PROTOCOL);

    OSPF_EXT_PROTOCOLS = ImmutableSet.of(OSPF_EXT1_PROTOCOL, OSPF_EXT2_PROTOCOL);

    OSPF_PROTOCOLS =
        ImmutableSet.<RoutingProtocol>builder()
            .addAll(OSPF_EXT_PROTOCOLS)
            .addAll(OSPF_INT_PROTOCOLS)
            .build();

    ISIS_PROTOCOLS = ImmutableSet.of(ISIS_L1_PROTOCOL, ISIS_L2_PROTOCOL);

    EIGRP_PROTOCOLS = ImmutableSet.of(EIGRP_INT_PROTOCOL, EIGRP_EXT_PROTOCOL);

    IGP_PROTOCOLS =
        ImmutableSet.<RoutingProtocol>builder()
            .addAll(OSPF_PROTOCOLS)
            .addAll(ISIS_PROTOCOLS)
            .addAll(EIGRP_PROTOCOLS)
            .add(RIP_PROTOCOL)
            .build();

    BGP_PROTOCOLS = ImmutableSet.of(EBGP_PROTOCOL, IBGP_PROTOCOL);

    MAP =
        ImmutableMap.<String, Set<RoutingProtocol>>builder()
            .put(AGGREGATE, ImmutableSet.of(AGGREGATE_PROTOCOL))
            .put(ALL, ALL_PROTOCOLS)
            .put(BGP, BGP_PROTOCOLS)
            .put(CONNECTED, ImmutableSet.of(CONNECTED_PROTOCOL))
            .put(EBGP, ImmutableSet.of(EBGP_PROTOCOL))
            .put(EIGRP, EIGRP_PROTOCOLS)
            .put(EIGRP_EXT, ImmutableSet.of(EIGRP_EXT_PROTOCOL))
            .put(EIGRP_INT, ImmutableSet.of(EIGRP_INT_PROTOCOL))
            .put(IBGP, ImmutableSet.of(IBGP_PROTOCOL))
            .put(IGP, IGP_PROTOCOLS)
            .put(ISIS, ISIS_PROTOCOLS)
            .put(ISIS_L1, ImmutableSet.of(ISIS_L1_PROTOCOL))
            .put(ISIS_L2, ImmutableSet.of(ISIS_L2_PROTOCOL))
            .put(LOCAL, ImmutableSet.of(LOCAL_PROTOCOL))
            .put(OSPF, OSPF_PROTOCOLS)
            .put(OSPF_EXT, OSPF_EXT_PROTOCOLS)
            .put(OSPF_EXT1, ImmutableSet.of(OSPF_EXT1_PROTOCOL))
            .put(OSPF_EXT2, ImmutableSet.of(OSPF_EXT2_PROTOCOL))
            .put(OSPF_INT, OSPF_INT_PROTOCOLS)
            .put(OSPF_INTER, ImmutableSet.of(OSPF_INTER_PROTOCOL))
            .put(OSPF_INTRA, ImmutableSet.of(OSPF_INTRA_PROTOCOL))
            .put(RIP, ImmutableSet.of(RIP_PROTOCOL))
            .put(STATIC, ImmutableSet.of(STATIC_PROTOCOL))
            .build();

    ALL_PROTOCOLS_SPECIFIER = new RoutingProtocolSpecifier(ALL);
  }

  private final String _text;

  private final Set<RoutingProtocol> _protocols;

  @JsonCreator
  @VisibleForTesting
  static RoutingProtocolSpecifier create(@Nullable String text) {
    return new RoutingProtocolSpecifier(firstNonNull(text, ALL));
  }

  private static Set<RoutingProtocol> fromString(String text) {
    String[] values = text.trim().split(",");
    return Arrays.stream(values)
        .map(String::trim)
        .map(String::toLowerCase)
        .map(MAP::get)
        .filter(Objects::nonNull)
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @JsonValue
  public @Nonnull String value() {
    return _text;
  }

  public RoutingProtocolSpecifier(String text) {
    _text = text;
    _protocols = fromString(text);
  }

  /**
   * Returns a list of suggestions based on the query, based on {@link
   * PropertySpecifier#baseAutoComplete}.
   */
  public static List<AutocompleteSuggestion> autoComplete(String query) {
    return PropertySpecifier.baseAutoComplete(query, MAP.keySet());
  }

  public Set<RoutingProtocol> getProtocols() {
    return _protocols;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoutingProtocolSpecifier)) {
      return false;
    }
    RoutingProtocolSpecifier that = (RoutingProtocolSpecifier) o;
    return Objects.equals(_protocols, that._protocols) && _text.equals(that._text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_protocols, _text);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("stringValue", _text)
        .add("protocols", _protocols)
        .toString();
  }
}
