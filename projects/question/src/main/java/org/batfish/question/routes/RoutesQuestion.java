package org.batfish.question.routes;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.routes.RoutesQuestion.RibProtocol.MAIN;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/** Returns computed routes after dataplane computation. */
@ParametersAreNonnullByDefault
public class RoutesQuestion extends Question {

  /** RIBs of these protocols are available for examining routes using {@link RoutesQuestion}. */
  public enum RibProtocol {
    MAIN("main"),
    BGP("bgp"),
    BGPMP("bgpmp"); // BGP multi path

    private final String _protocolName;

    private static final Map<String, RibProtocol> _map = buildMap();

    private static Map<String, RibProtocol> buildMap() {
      return Arrays.stream(RibProtocol.values())
          .collect(
              ImmutableMap.toImmutableMap(p -> p._protocolName.toLowerCase(), Function.identity()));
    }

    @JsonCreator
    private static RibProtocol fromName(String name) {
      return _map.getOrDefault(name.toLowerCase(), MAIN);
    }

    RibProtocol(String protocol) {
      _protocolName = protocol;
    }
  }

  private static final String PROP_NODE_REGEX = "nodeRegex";

  private static final String PROP_VRF_REGEX = "vrfRegex";

  private static final String PROP_PROTOCOL = "protocol";

  private static final String QUESTION_NAME = "routes2";

  @Nonnull private NodesSpecifier _nodeRegex;

  @Nonnull private String _vrfRegex;

  @Nonnull private RibProtocol _protocol;

  /**
   * Create a new question.
   *
   * @param nodeRegex {@link NodesSpecifier} indicating which nodes' RIBs should be considered
   * @param vrfRegex a regex pattern indicating which VRFs should be considered
   * @param protocol a specific protocol RIB to return routes from.
   */
  @JsonCreator
  private RoutesQuestion(
      @Nullable @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex,
      @Nullable @JsonProperty(PROP_VRF_REGEX) String vrfRegex,
      @Nullable @JsonProperty(PROP_PROTOCOL) RibProtocol protocol) {
    _nodeRegex = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    _vrfRegex = firstNonNull(vrfRegex, ".*");
    _protocol = firstNonNull(protocol, MAIN);
  }

  /** Create new routes question with default parameters. */
  public RoutesQuestion() {
    this(null, null, null);
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return QUESTION_NAME;
  }

  @JsonProperty(PROP_NODE_REGEX)
  @Nonnull
  public NodesSpecifier getNodeRegex() {
    return _nodeRegex;
  }

  @JsonProperty(PROP_VRF_REGEX)
  @Nonnull
  public String getVrfRegex() {
    return _vrfRegex;
  }

  @JsonProperty(PROP_PROTOCOL)
  @Nonnull
  public RibProtocol getProtocol() {
    return _protocol;
  }
}
