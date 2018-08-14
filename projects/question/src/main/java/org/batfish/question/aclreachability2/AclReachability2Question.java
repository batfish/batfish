package org.batfish.question.aclreachability2;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.batfish.specifier.FlexibleFilterSpecifierFactory;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;

/**
 * A question that returns unreachable lines of ACLs in a tabular format. {@link
 * AclReachability2Question#_filterSpecifier} determines which ACLs are checked, and {@link
 * AclReachability2Question#_nodeSpecifier} determines which nodes are checked for those ACLs.
 */
public class AclReachability2Question extends Question {
  private static final String DEFAULT_FILTER_SPECIFIER_FACTORY =
      FlexibleFilterSpecifierFactory.NAME;

  private static final String DEFAULT_NODE_SPECIFIER_FACTORY = FlexibleNodeSpecifierFactory.NAME;

  private static final String PROP_FILTER_SPECIFIER_FACTORY = "filterSpecifierFactory";

  private static final String PROP_FILTER_SPECIFIER = "filterSpecifier";

  private static final String PROP_NODE_SPECIFIER_FACTORY = "nodeSpecifierFactory";

  private static final String PROP_NODE_SPECIFIER = "nodeSpecifier";

  private String _filterSpecifierFactory;

  private String _filterSpecifier;

  private String _nodeSpecifierFactory;

  private String _nodeSpecifier;

  public AclReachability2Question() {
    this(null, null, null, null);
  }

  public AclReachability2Question(
      @Nullable @JsonProperty(PROP_FILTER_SPECIFIER_FACTORY) String filterSpecifierFactory,
      @Nullable @JsonProperty(PROP_FILTER_SPECIFIER) String filtersSpecifierInput,
      @Nullable @JsonProperty(PROP_NODE_SPECIFIER_FACTORY) String nodeSpecifierFactory,
      @Nullable @JsonProperty(PROP_NODE_SPECIFIER) String nodeSpecifierInput) {}

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_FILTER_SPECIFIER_FACTORY)
  public String getFilterSpecifierFactory() {
    return _filterSpecifierFactory;
  }

  @JsonProperty(PROP_FILTER_SPECIFIER)
  public String getFilterSpecifier() {
    return _filterSpecifier;
  }

  @Override
  public String getName() {
    return "aclreachability2";
  }

  @JsonProperty(PROP_NODE_SPECIFIER_FACTORY)
  public String getNodeSpecifierFactory() {
    return _nodeSpecifierFactory;
  }

  @JsonProperty(PROP_NODE_SPECIFIER)
  public String getNodeSpecifier() {
    return _nodeSpecifier;
  }

  @JsonIgnore
  public FilterSpecifier filterSpecifier() {
    return FilterSpecifierFactory.load(
            firstNonNull(_filterSpecifierFactory, DEFAULT_FILTER_SPECIFIER_FACTORY))
        .buildFilterSpecifier(_filterSpecifier);
  }

  @JsonIgnore
  public NodeSpecifier nodeSpecifier() {
    return NodeSpecifierFactory.load(
            firstNonNull(_nodeSpecifierFactory, DEFAULT_NODE_SPECIFIER_FACTORY))
        .buildNodeSpecifier(_nodeSpecifier);
  }
}
