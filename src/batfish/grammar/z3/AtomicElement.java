package batfish.grammar.z3;

public class AtomicElement implements Element {
   private String _text;
   
   public AtomicElement(String text) {
      _text = text;
   }
   
   public String getText() {
      return _text;
   }
   
   @Override
   public String toString() {
      return _text;
   }

   @Override
   public void prettyPrint(StringBuilder sb, int indent, String[] vars) {
      sb.append(_text);
   }
}
