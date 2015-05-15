package org.batfish.grammar.question;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.BatfishExtractor;
import org.batfish.grammar.question.QuestionParser.Multipath_questionContext;
import org.batfish.question.Question;
import org.batfish.question.QuestionType;

public class QuestionExtractor extends QuestionParserBaseListener implements
      BatfishExtractor {

   private Question _question;

   @Override
   public void enterMultipath_question(Multipath_questionContext ctx) {
      _question = new Question(QuestionType.MULTIPATH);
      _question.setMasterEnvironment(ctx.environment.getText());
   }

   public Question getQuestion() {
      return _question;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(this, tree);
   }

}
