package batfish.grammar.z3;

import batfish.z3.Synthesizer;

public class MultilineList extends ListElement {

   @Override
   public void prettyPrint(StringBuilder sb, int indent, String[] vars) {
      sb.append("(");
      _headElement.prettyPrint(sb, indent, vars);
      int bodyIndent = indent + 1;
      for (Element bodyElement : _bodyElements) {
         sb.append("\n" + Synthesizer.indent(bodyIndent));
         bodyElement.prettyPrint(sb, bodyIndent, vars);
      }
      sb.append(")");
   }

}
