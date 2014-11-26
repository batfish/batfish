package batfish.representation.juniper;

public class StaticOptions_Discard extends StaticOptions {

   private boolean _discard;

   public StaticOptions_Discard(boolean b) {
      _discard = b;
   }

   public boolean getDiscard() {
      return _discard;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.DISCARD;
   }

}
