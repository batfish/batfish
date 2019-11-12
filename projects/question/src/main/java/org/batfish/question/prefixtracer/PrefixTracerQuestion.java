package org.batfish.question.prefixtracer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierFactories;

/**
 * Question that explores how a particular network prefix is propagated through the network.
 * Currently, only tracing/propagation via BGP is supported.
 */
public class PrefixTracerQuestion extends Question {
  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_NODES = "nodes";

  @Nullable private String _nodes;
  @Nullable private Prefix _prefix;

  @JsonCreator
  private static PrefixTracerQuestion create(
      @JsonProperty(PROP_PREFIX) Prefix prefix, @Nullable @JsonProperty(PROP_NODES) String nodes) {
    return new PrefixTracerQuestion(prefix, nodes);
  }

  public PrefixTracerQuestion(@Nullable Prefix prefix, @Nullable String nodes) {
    _prefix = prefix;
    _nodes = nodes;
  }

  public PrefixTracerQuestion() {
    this(null, null);
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
    return "prefixTracer";
  }

  @Nullable
  @JsonProperty(PROP_PREFIX)
  public Prefix getPrefix() {
    return _prefix;
  }

  @Nullable
  @JsonProperty(PROP_NODES)
  public String getNodes() {
    return _nodes;
  }

  @Nonnull
  @JsonIgnore
  public NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }
}
