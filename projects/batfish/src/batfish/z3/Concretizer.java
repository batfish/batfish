package batfish.z3;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.z3.AtomicElement;
import batfish.grammar.z3.Element;
import batfish.grammar.z3.MultilineList;
import batfish.grammar.z3.Result;

public class Concretizer {

   private List<Result> _results;
   private String[] _variables;

   public Concretizer(List<Result> results, String[] variables) {
      _results = results;
      _variables = variables;
   }

   public List<String> concretize() {
      List<String> output = new ArrayList<String>();
      StringBuilder sb = new StringBuilder();
      sb.append(Synthesizer.getPacketVarDecls());
      for (Result result : _results) {
         Element e = result.getElement();
         MultilineList assertion = new MultilineList();
         assertion.setHeadElement(new AtomicElement("assert"));
         assertion.addBodyElement(e);
         assertion.prettyPrint(sb, 0, _variables);
         sb.append("\n");
      }
      sb.append("(check-sat)\n");
      sb.append("(get-model)\n");
      String text = sb.toString();
      output.add(text);
      return output;
   }

}
