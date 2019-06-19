package org.batfish.main;

import java.util.function.BiFunction;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.questions.Question;

/** Functions that create {@link Answerer Answerers} for {@link Question Questions}. */
@ParametersAreNonnullByDefault
final class AnswererCreator {
  private final String _questionClassName;
  private final BiFunction<Question, IBatfish, Answerer> _creator;

  AnswererCreator(String questionClassName, BiFunction<Question, IBatfish, Answerer> creator) {
    _questionClassName = questionClassName;
    _creator = creator;
  }

  String getQuestionClassName() {
    return _questionClassName;
  }

  Answerer create(Question question, IBatfish batfish) {
    return _creator.apply(question, batfish);
  }
}
