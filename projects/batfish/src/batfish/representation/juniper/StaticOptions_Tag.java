package batfish.representation.juniper;

public class StaticOptions_Tag extends StaticOptions {

   private String _tag;

   public StaticOptions_Tag(String i) {
      _tag = i;
   }

   public String getTag() {
      return _tag;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.TAG;
   }

}
