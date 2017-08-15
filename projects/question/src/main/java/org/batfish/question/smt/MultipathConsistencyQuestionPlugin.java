package org.batfish.question.smt;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.question.QuestionPlugin;


public class MultipathConsistencyQuestionPlugin extends QuestionPlugin {

    public static class MulipathConsistencyAnswerer extends Answerer {

        public MulipathConsistencyAnswerer(Question question, IBatfish batfish) {
            super(question, batfish);
        }

        @Override
        public AnswerElement answer() {
            MultipathConsistencyQuestion q = (MultipathConsistencyQuestion) _question;
            return _batfish.smtMultipathConsistency(q);
        }
    }

    public static class MultipathConsistencyQuestion extends HeaderLocationQuestion {

        @Override
        public boolean getDataPlane() {
            return false;
        }

        @Override
        public String getName() {
            return "smt-multipath-consistency";
        }
    }


    @Override
    protected Answerer createAnswerer(Question question, IBatfish batfish) {
        return new MulipathConsistencyAnswerer(question, batfish);
    }

    @Override
    protected Question createQuestion() {
        return new MultipathConsistencyQuestion();
    }
}
