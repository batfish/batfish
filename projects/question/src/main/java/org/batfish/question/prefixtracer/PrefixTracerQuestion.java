package org.batfish.question.prefixtracer;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * Question that explores how a particular network prefix is propagated through the network.
 * Currently, only tracing/propagation via BGP is supported.
 */
public class PrefixTracerQuestion extends Question {

  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_NODE_REGEX = "nodeRegex";

  @Nullable private Prefix _prefix;
  private NodesSpecifier _nodeSpecifier;

  @JsonCreator
  private PrefixTracerQuestion(
      @JsonProperty(PROP_PREFIX) Prefix prefix,
      @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex) {
    _prefix = prefix;
    _nodeSpecifier = firstNonNull(nodeRegex, NodesSpecifier.ALL);
  }

  public PrefixTracerQuestion() {
    this(null, NodesSpecifier.ALL);
  }

  /** Returns {@code true} iff this question requires a computed data plane as input. */
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
    return "prefixtracer";
  }

  @Nullable
  @JsonProperty(PROP_PREFIX)
  public Prefix getPrefix() {
    return _prefix;
  }

  @JsonProperty(PROP_NODE_REGEX)
  public NodesSpecifier getNodeRegex() {
    return _nodeSpecifier;
  }
}
