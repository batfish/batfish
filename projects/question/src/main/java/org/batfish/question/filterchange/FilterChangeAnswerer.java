package org.batfish.question.filterchange;

import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.searchfilters.SearchFiltersQuestion;

@ParametersAreNonnullByDefault
public class FilterChangeAnswerer extends Answerer {

  public FilterChangeAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public FilterChangeAnswerElement answer() {
    FilterChangeQuestion question = (FilterChangeQuestion) _question;

    String filters = question.getFilterSpecifierInput();
    String nodes = question.getNodeSpecifierInput();
    PacketHeaderConstraints headerConstraints = question.getHeaderConstraints();
    LineAction action = question.getAction();
    boolean generateExplanations = question.getGenerateExplanations();

    // check whether any of the intended changes is already satisfied in the original network.
    // we assume that the original network is installed as the delta snapshot.
    _batfish.pushDeltaSnapshot();
    SearchFiltersQuestion redundancyFlowsCheck =
        SearchFiltersQuestion.builder()
            .setFilterSpecifier(filters)
            .setNodeSpecifier(nodes)
            .setHeaders(headerConstraints)
            .setAction(lineActionToSearchFiltersTypeString(action))
            .setGenerateExplanations(generateExplanations)
            .build();
    TableAnswerElement redundantFlows =
        (TableAnswerElement) _batfish.createAnswerer(redundancyFlowsCheck).answer();
    _batfish.popSnapshot();

    // check whether any of the intended changes is NOT satisfied in the changed network.
    // we assume that the changed network is installed as the base snapshot.
    _batfish.pushBaseSnapshot();
    SearchFiltersQuestion incorrectFlowsCheck =
        SearchFiltersQuestion.builder()
            .setFilterSpecifier(filters)
            .setNodeSpecifier(nodes)
            .setHeaders(headerConstraints)
            .setAction(lineActionToSearchFiltersTypeString(negateLineAction(action)))
            .setGenerateExplanations(generateExplanations)
            .build();
    TableAnswerElement incorrectFlows =
        (TableAnswerElement) _batfish.createAnswerer(incorrectFlowsCheck).answer();
    _batfish.popSnapshot();

    // check whether anything outside of the intended change was modified.
    SearchFiltersQuestion collateralDamageCheck =
        SearchFiltersQuestion.builder()
            .setFilterSpecifier(filters)
            .setNodeSpecifier(nodes)
            .setHeaders(headerConstraints)
            .setComplementHeaderSpace(true)
            .build();
    TableAnswerElement collateralDamage =
        (TableAnswerElement) _batfish.createAnswerer(collateralDamageCheck).answerDiff();

    return new FilterChangeAnswerElement(redundantFlows, incorrectFlows, collateralDamage);
  }

  private static LineAction negateLineAction(LineAction action) {
    switch (action) {
      case DENY:
        return PERMIT;
      case PERMIT:
        return DENY;
      default:
        throw new BatfishException("Unexpected line aaction " + action);
    }
  }

  private static String lineActionToSearchFiltersTypeString(LineAction action) {
    switch (action) {
      case DENY:
        return "deny";
      case PERMIT:
        return "permit";
      default:
        throw new BatfishException("Unexpected line action " + action);
    }
  }
}
