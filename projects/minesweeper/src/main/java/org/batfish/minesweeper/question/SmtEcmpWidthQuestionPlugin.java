package org.batfish.minesweeper.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.minesweeper.smt.PropertyChecker;
import org.batfish.question.QuestionPlugin;

@AutoService(Plugin.class)
public class SmtEcmpWidthQuestionPlugin extends QuestionPlugin {

    public static class EcmpWidthAnswerer extends Answerer {

        public EcmpWidthAnswerer(Question question, IBatfish batfish) {
            super(question, batfish);
        }

        @Override
        public AnswerElement answer() {
            EcmpWidthQuestion q = (EcmpWidthQuestion) _question;
            PropertyChecker p = new PropertyChecker(new BDDPacket(), _batfish);
            return p.checkEcmpWidth(q);
        }
    }

    public static class EcmpWidthQuestion extends HeaderLocationQuestion {

        @Override
        public boolean getDataPlane() {
            return false;
        }

        @Override
        public String getName() {
            return "smt-ecmp-width";
        }
    }

    @Override
    protected Answerer createAnswerer(Question question, IBatfish batfish) {
        return new EcmpWidthAnswerer(question, batfish);
    }

    @Override
    protected Question createQuestion() {
        return new EcmpWidthQuestion();
    }
}