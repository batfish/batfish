package org.batfish.client;

import java.util.Map;
import java.util.function.Supplier;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.Question;

public class QuestionHelper {

  public static String getParametersString(Map<String, String> parameters) {
    String retString = "{\n";

    for (String paramKey : parameters.keySet()) {
      retString += String.format("\"%s\" : %s,\n", paramKey, parameters.get(paramKey));
    }

    retString += "}\n";

    return retString;
  }

  public static Question getQuestion(
      String questionTypeStr, Map<String, Supplier<Question>> questions) {
    Supplier<Question> supplier = questions.get(questionTypeStr);
    if (supplier == null) {
      throw new BatfishException("No question found of type: " + questionTypeStr + '.');
    }
    Question question = supplier.get();
    return question;
  }

  public static String getQuestionString(
      String questionTypeStr, Map<String, Supplier<Question>> questions, boolean full) {
    Question question = getQuestion(questionTypeStr, questions);
    if (full) {
      return question.toFullJsonString();
    } else {
      return question.toJsonString();
    }
  }
}
