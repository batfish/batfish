package org.batfish.question.vxlanproperties;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/** A question that returns a table with VXLAN network segments and their properties. */
public final class VxlanVniPropertiesQuestion extends Question {

  private static final String PROP_NODES = "nodes";

  @Nonnull private NodesSpecifier _nodes;

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "vxlanVniProperties";
  }

  VxlanVniPropertiesQuestion(@JsonProperty(PROP_NODES) NodesSpecifier nodeRegex) {
    _nodes = firstNonNull(nodeRegex, NodesSpecifier.ALL);
  }

  @JsonProperty(PROP_NODES)
  public NodesSpecifier getNodes() {
    return _nodes;
  }
}
