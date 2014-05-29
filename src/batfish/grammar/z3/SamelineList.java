package batfish.grammar.z3;

public class SamelineList extends ListElement {

   @Override
   public void prettyPrint(StringBuilder sb, int indent, String[] vars) {
      sb.append("(");
      _headElement.prettyPrint(sb, indent, vars);
      for (Element bodyElement : _bodyElements) {
         sb.append(" ");
         bodyElement.prettyPrint(sb, indent, vars);
      }
      sb.append(")");
   }

}
