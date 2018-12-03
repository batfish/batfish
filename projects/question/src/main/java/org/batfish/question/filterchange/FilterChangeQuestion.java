package org.batfish.question.filterchange;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;

/**
 * A question that checks whether an ACL change was necessary, achieved the intended goal, and did
 * not cause any collateral damage.
 */
public class FilterChangeQuestion extends Question {

  private static final String PROP_FILTER_SPECIFIER_INPUT = "filters";

  private static final String PROP_NODE_SPECIFIER_INPUT = "nodes";

  private static final String PROP_HEADER_CONSTRAINTS = "headerConstraints";

  private static final String PROP_ACTION = "action";

  private static final String PROP_GENERATE_EXPLANATIONS = "generateExplanations";

  @Nullable private String _filterSpecifierInput;

  @Nullable private String _nodeSpecifierInput;

  @Nonnull private PacketHeaderConstraints _headerConstraints;

  @Nonnull private LineAction _action;

  private boolean _generateExplanations;

  public FilterChangeQuestion() {
    this(null, null, null, null, false);
  }

  public FilterChangeQuestion(
      @Nullable @JsonProperty(PROP_FILTER_SPECIFIER_INPUT) String filtersSpecifierInput,
      @Nullable @JsonProperty(PROP_NODE_SPECIFIER_INPUT) String nodeSpecifierInput,
      @Nullable @JsonProperty(PROP_HEADER_CONSTRAINTS) PacketHeaderConstraints headerConstraints,
      @Nullable @JsonProperty(PROP_ACTION) LineAction action,
      @JsonProperty(PROP_GENERATE_EXPLANATIONS) boolean generateExplanations) {
    setDifferential(true);
    _filterSpecifierInput = filtersSpecifierInput;
    _nodeSpecifierInput = nodeSpecifierInput;
    _headerConstraints = firstNonNull(headerConstraints, PacketHeaderConstraints.unconstrained());
    _action = firstNonNull(action, LineAction.PERMIT);
    _generateExplanations = generateExplanations;
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @JsonProperty(PROP_FILTER_SPECIFIER_INPUT)
  @Nullable
  public String getFilterSpecifierInput() {
    return _filterSpecifierInput;
  }

  @JsonProperty(PROP_GENERATE_EXPLANATIONS)
  public boolean getGenerateExplanations() {
    return _generateExplanations;
  }

  @Override
  public String getName() {
    return "filterChange";
  }

  @JsonProperty(PROP_NODE_SPECIFIER_INPUT)
  @Nullable
  public String getNodeSpecifierInput() {
    return _nodeSpecifierInput;
  }

  @JsonProperty(PROP_HEADER_CONSTRAINTS)
  @Nonnull
  public PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
  }

  @JsonProperty(PROP_ACTION)
  @Nonnull
  public LineAction getAction() {
    return _action;
  }
}
