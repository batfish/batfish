package batfish.grammar.z3;

import java.util.Map;

public class ConcretizerQueryResultExtractor extends ConcretizerQueryResultParserBaseListener {

   private Map<String, Long> _constraints;
   private String _node;

   public Map<String, Long> getConstraints() {
      return _constraints;
   }

   public String getNode() {
      return _node;
   }

}
