package batfish.representation.juniper;

import java.io.Serializable;

public class GenerateRoute implements Serializable {

   private static final long serialVersionUID = 1L;

   private String _prefix;
   private int _prefixLength;
   private String _policy;
   private int  _preference;

   public GenerateRoute(String prefix, int prefixLength, String policy, int distance) {
      _prefix = prefix;
      _prefixLength = prefixLength;
      _policy = policy;
      _preference = distance;
   }
   
   public void setPolicy(String p){
      _policy = p;
   }

   public String getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public String getPolicy() {
      return _policy;
   }

   public int getPreference() {
      return _preference;
   }
}
