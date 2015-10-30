package org.batfish.grammar.question;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.BatfishException;
import org.batfish.grammar.BatfishExtractor;
import org.batfish.grammar.question.QuestionParametersParser.BindingContext;
import org.batfish.grammar.question.QuestionParametersParser.ParametersContext;
import org.batfish.question.QuestionParameters;
import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;

public class QuestionParametersExtractor extends
      QuestionParametersParserBaseListener implements BatfishExtractor {

   private QuestionParameters _parameters;

   @Override
   public void enterBinding(BindingContext ctx) {
      String var = ctx.VARIABLE().getText();
      VariableType type;
      Object value;
      if (ctx.integer() != null) {
         type = VariableType.INT;
         value = Long.parseLong(ctx.integer().getText());
      }
      else if (ctx.IP_ADDRESS() != null) {
         type = VariableType.IP;
         value = new Ip(ctx.IP_ADDRESS().getText());
      }
      else if (ctx.IP_PREFIX() != null) {
         type = VariableType.PREFIX;
         value = new Prefix(ctx.IP_PREFIX().getText());
      }
      else if (ctx.REGEX() != null) {
         type = VariableType.REGEX;
         value = ctx.REGEX().getText();
      }
      else if (ctx.STRING_LITERAL() != null) {
         type = VariableType.STRING;
         value = ctx.STRING_LITERAL().getText();
      }
      else {
         throw new BatfishException("Invalid binding for variable: \"" + var
               + "\"");
      }
      if (_parameters.getTypeBindings().get(var) != null) {
         throw new BatfishException("Duplicate assignment to variable: \""
               + var + "\"");
      }
      _parameters.getTypeBindings().put(var, type);
      _parameters.getStore().put(var, value);
   }

   @Override
   public void enterParameters(ParametersContext ctx) {
      _parameters = new QuestionParameters();
   }

   public QuestionParameters getParameters() {
      return _parameters;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(this, tree);
   }

}
