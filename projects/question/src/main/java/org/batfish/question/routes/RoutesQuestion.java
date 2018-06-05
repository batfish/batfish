package org.batfish.question.routes;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/** Returns computed routes after dataplane computation. */
@ParametersAreNonnullByDefault
public class RoutesQuestion extends Question {

  private static final String PROP_NODE_REGEX = "nodeRegex";

  private static final String PROP_VRF_REGEX = "vrfRegex";

  private static final String PROP_PROTOCOL = "protocol";

  private static final String QUESTION_NAME = "routes2";

  @Nonnull private NodesSpecifier _nodeRegex;

  @Nonnull private String _vrfRegex;

  @Nonnull private String _protocol;

  @JsonCreator
  private RoutesQuestion(
      @Nullable @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex,
      @Nullable @JsonProperty(PROP_VRF_REGEX) String vrfRegex,
      @Nullable @JsonProperty(PROP_PROTOCOL) String protocol) {
    _nodeRegex = firstNonNull(nodeRegex, NodesSpecifier.ALL);
    _vrfRegex = firstNonNull(vrfRegex, ".*");
    _protocol = firstNonNull(protocol, "all");
  }

  /** Create new routes question with default parameters. */
  public RoutesQuestion() {
    this(null, null, null);
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  /**
   * Returns the short name of this question, used in place of the classname to identify this
   * question.
   */
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
  public String getProtocol() {
    return _protocol;
  }
}
