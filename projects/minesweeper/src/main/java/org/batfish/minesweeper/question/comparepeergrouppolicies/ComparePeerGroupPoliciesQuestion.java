package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import org.batfish.datamodel.questions.Question;

public class ComparePeerGroupPoliciesQuestion extends Question {

  public ComparePeerGroupPoliciesQuestion() {}

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "SemDiff";
  }
}
