package org.batfish.question.bgpproperties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;

/**
 * A question that returns properties of BGP routing processes. {@link #_nodes} determines which
 * nodes are included. The default is to include everything.
 */
public class BgpPeerConfigurationQuestion extends Question {

  private static final String PROP_NODES = "nodes";

  @Nullable private String _nodes;

  @JsonCreator
  public BgpPeerConfigurationQuestion(@JsonProperty(PROP_NODES) @Nullable String nodes) {
    _nodes = nodes;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "bgpPeerConfiguration";
  }

  @JsonProperty(PROP_NODES)
  @Nullable
  public String getNodes() {
    return _nodes;
  }

  @Nonnull
  NodeSpecifier getNodesSpecifier() {
    return NodeSpecifierFactory.load(FlexibleNodeSpecifierFactory.NAME).buildNodeSpecifier(_nodes);
  }
}
