package org.batfish.question.smt;

import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.question.QuestionPlugin;


public class ReachabilityQuestionPlugin extends QuestionPlugin {

    @Override
    protected Answerer createAnswerer(Question question, IBatfish batfish) {
        return new ReachabilityAnswerer(question, batfish);
    }

    @Override
    protected Question createQuestion() {
        return new ReachabilityQuestion();
    }

    public static class ReachabilityAnswerer extends Answerer {

        public ReachabilityAnswerer(Question question, IBatfish batfish) {
            super(question, batfish);
        }

        @Override
        public AnswerElement answer() {
            ReachabilityQuestion q = (ReachabilityQuestion) _question;
            return _batfish.smtReachability(q);
        }
    }

    public static class ReachabilityQuestion extends HeaderLocationQuestion {

        @Override
        public boolean getDataPlane() {
            return false;
        }

        @Override
        public String getName() {
            return "smt-reachability";
        }

    }
}
