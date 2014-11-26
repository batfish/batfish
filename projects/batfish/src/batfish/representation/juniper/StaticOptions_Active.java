package batfish.representation.juniper;

public class StaticOptions_Active extends StaticOptions {

   private boolean _active;

   public StaticOptions_Active(boolean b) {
      _active = b;
   }

   public boolean getActive() {
      return _active;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.ACTIVE_PASSIVE;
   }

}
