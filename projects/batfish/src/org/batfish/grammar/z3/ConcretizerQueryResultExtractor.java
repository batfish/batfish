package org.batfish.grammar.z3;

import java.util.HashMap;
import java.util.Map;

import org.batfish.grammar.z3.ConcretizerQueryResultParser.*;

public class ConcretizerQueryResultExtractor extends
      ConcretizerQueryResultParserBaseListener {

   private Map<String, Long> _constraints;
   private String _id;

   public ConcretizerQueryResultExtractor() {
      _constraints = new HashMap<String, Long>();
   }

   public void exitDefine_fun(Define_funContext ctx) {
      String var = ctx.var.getText();
      long value;
      if (ctx.BIN() != null) {
         String binaryPortion = ctx.BIN().getText().substring(2);
         value = Long.parseLong(binaryPortion, 2);
      }
      else if (ctx.HEX() != null) {
         String hexPortion = ctx.HEX().getText().substring(2);
         value = Long.parseLong(hexPortion, 16);
      }
      else {
         throw new Error("bad value");
      }
      _constraints.put(var, value);
   }

   public void exitResult(ResultContext ctx) {
      if (ctx.id != null) {
         _id = ctx.id.getText();
      }
   }

   public Map<String, Long> getConstraints() {
      return _constraints;
   }

   public String getId() {
      return _id;
   }

}
