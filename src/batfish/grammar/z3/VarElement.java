package batfish.grammar.z3;

public class VarElement implements Element {
   private int _num;

   public VarElement(int num) {
      _num = num;
   }
   
   @Override
   public String toString() {
      return ":VAR-" + _num;
   }

   @Override
   public void prettyPrint(StringBuilder sb, int indent, String[] vars) {
      String varName = vars[_num];
      sb.append(varName);
   }

}
