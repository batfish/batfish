package batfish.representation.juniper;

public class StaticOptions_AsPath extends StaticOptions {

   private String _asPath;

   public StaticOptions_AsPath(String a) {
      _asPath = a;
   }

   public String getAsPath() {
      return _asPath;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.ASPATH;
   }

}
