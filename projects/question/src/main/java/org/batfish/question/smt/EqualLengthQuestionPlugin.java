package org.batfish.question.smt;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.question.QuestionPlugin;


public class EqualLengthQuestionPlugin extends QuestionPlugin {

    public static class EqualLengthAnswerer extends Answerer {

        public EqualLengthAnswerer(Question question, IBatfish batfish) {
            super(question, batfish);
        }

        @Override
        public AnswerElement answer() {
            EqualLengthQuestion q = (EqualLengthQuestion) _question;
            return _batfish.smtEqualLength(q);
        }
    }

    public static class EqualLengthQuestion extends HeaderLocationQuestion {

        @Override
        public boolean getDataPlane() {
            return false;
        }

        @Override
        public String getName() {
            return "smt-equal-length";
        }
    }


    @Override
    protected Answerer createAnswerer(Question question, IBatfish batfish) {
        return new EqualLengthAnswerer(question, batfish);
    }

    @Override
    protected Question createQuestion() {
        return new EqualLengthQuestion();
    }
}
