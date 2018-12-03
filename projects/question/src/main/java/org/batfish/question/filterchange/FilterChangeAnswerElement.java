package org.batfish.question.filterchange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.table.TableAnswerElement;

public class FilterChangeAnswerElement extends AnswerElement {

  private static final String PROP_REDUNDANT_FLOWS = "redundantFlows";

  private static final String PROP_INCORRECT_FLOWS_ = "incorrectFlows";

  private static final String PROP_COLLATERAL_DAMAGE = "collateralDamage";

  @Nonnull private TableAnswerElement _redundantFlows;

  @Nonnull private TableAnswerElement _incorrectFlows;

  @Nonnull private TableAnswerElement _collateralDamage;

  @JsonCreator
  public FilterChangeAnswerElement(
      @Nonnull @JsonProperty(PROP_REDUNDANT_FLOWS) TableAnswerElement redundantFlows,
      @Nonnull @JsonProperty(PROP_INCORRECT_FLOWS_) TableAnswerElement incorrectFlows,
      @Nonnull @JsonProperty(PROP_COLLATERAL_DAMAGE) TableAnswerElement collateralDamage) {
    _redundantFlows = redundantFlows;
    _incorrectFlows = incorrectFlows;
    _collateralDamage = collateralDamage;
  }

  @JsonProperty(PROP_REDUNDANT_FLOWS)
  public TableAnswerElement getRedundantFlows() {
    return _redundantFlows;
  }

  @JsonProperty(PROP_INCORRECT_FLOWS_)
  public TableAnswerElement getIncorrectFlows() {
    return _incorrectFlows;
  }

  @JsonProperty(PROP_COLLATERAL_DAMAGE)
  public TableAnswerElement getCollateralDamage() {
    return _collateralDamage;
  }
}
