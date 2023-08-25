package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.semdiff;

import org.batfish.datamodel.questions.Question;

public class SemDiffQuestion extends Question {

  public SemDiffQuestion() {}

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "SemDiff";
  }
}
