package org.batfish.client;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.batfish.client.answer.LoadQuestionAnswerElement;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.Question;

public class QuestionHelper {

  public static String getParametersString(Map<String, String> parameters) throws Exception {
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
      throw new BatfishException(
          "No question found of type: "
              + questionTypeStr
              + ". Did you include the questions plugins directory in your JVM arguments?");
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

  /**
   * Merges local and remote questions and overwrites remote with local
   *
   * @param localQuestions Questions from disk
   * @param remoteQuestions Questions from Coordinator
   * @param bfQuestions Batfish Questions
   * @param ae Answer Element
   * @return Merged {@link Map}
   */
  public static LoadQuestionAnswerElement mergeQuestions(
      Map<String, String> localQuestions,
      Map<String, String> remoteQuestions,
      Map<String, String> bfQuestions,
      LoadQuestionAnswerElement ae) {
    //merging remote questions
    for (Entry<String, String> question : remoteQuestions.entrySet()) {
      if (bfQuestions.containsKey(question.getKey().toLowerCase())) {
        ae.getReplaced().add(question.getKey());
      }
      bfQuestions.put(question.getKey().toLowerCase(), question.getValue());
    }
    //merging local questions
    for (Entry<String, String> question : localQuestions.entrySet()) {
      if (bfQuestions.containsKey(question.getKey().toLowerCase())) {
        ae.getReplaced().add(question.getKey());
      }
      bfQuestions.put(question.getKey().toLowerCase(), question.getValue());
    }
    return ae;
  }
}
