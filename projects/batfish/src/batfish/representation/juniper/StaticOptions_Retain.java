package batfish.representation.juniper;

public class StaticOptions_Retain extends StaticOptions {

   private boolean _retain;

   public StaticOptions_Retain(boolean b) {
      _retain = b;
   }

   public boolean getRetain() {
      return _retain;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.RETAIN_ORNO;
   }

}
