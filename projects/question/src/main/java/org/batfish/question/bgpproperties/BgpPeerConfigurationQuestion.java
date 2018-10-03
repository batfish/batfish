package org.batfish.question.bgpproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns properties of BGP routing processes. {@link #_nodes} determines which
 * nodes are included. The default is to include everything.
 */
public class BgpPeerConfigurationQuestion extends Question {

  private static final String PROP_NODES = "nodes";

  @Nonnull private NodesSpecifier _nodes;

  public BgpPeerConfigurationQuestion(@JsonProperty(PROP_NODES) NodesSpecifier nodes) {
    _nodes = firstNonNull(nodes, NodesSpecifier.ALL);
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
  public NodesSpecifier getNodes() {
    return _nodes;
  }
}
