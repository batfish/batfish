package org.batfish.question.aclreachability2;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

/**
 * A question that returns properties of nodes in a tabular format. {@link
 * AclReachability2Question#_aclNameRegex} determines which properties are included, and {@link
 * AclReachability2Question#_nodeRegex} determines which nodes are included.
 */
public class AclReachability2Question extends Question {

  private static final String PROP_ACL_NAME_REGEX = "aclNameRegex";
  private static final String PROP_NODE_REGEX = "nodeRegex";

  private String _aclNameRegex;
  @Nonnull private NodesSpecifier _nodeRegex;

  public AclReachability2Question() {
    this(".*", NodesSpecifier.ALL);
  }

  public AclReachability2Question(
      @JsonProperty(PROP_ACL_NAME_REGEX) String aclNameRegex,
      @JsonProperty(PROP_NODE_REGEX) NodesSpecifier nodeRegex) {
    _aclNameRegex = aclNameRegex;
    _nodeRegex = firstNonNull(nodeRegex, NodesSpecifier.ALL);
  }

  @JsonProperty(PROP_ACL_NAME_REGEX)
  public String getAclNameRegex() {
    return _aclNameRegex;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "aclreachability2";
  }

  @JsonProperty(PROP_NODE_REGEX)
  public NodesSpecifier getNodeRegex() {
    return _nodeRegex;
  }

  @JsonProperty(PROP_ACL_NAME_REGEX)
  public void setAclNameRegex(String regex) {
    _aclNameRegex = regex;
  }

  @JsonProperty(PROP_NODE_REGEX)
  public void setNodeRegex(NodesSpecifier regex) {
    _nodeRegex = regex;
  }
}
