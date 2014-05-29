package batfish.grammar.z3;

public class Constraint {
   private String _key;
   private long _value;

   public Constraint(String key, long value) {
      _key = key;
      _value = value;
   }

   public String getKey() {
      return _key;
   }

   public long getValue() {
      return _value;
   }
}
