package batfish.representation.juniper;

public class StaticOptions_Readvertise extends StaticOptions {

   private boolean _read;

   public StaticOptions_Readvertise(boolean b) {
      _read = b;
   }

   public boolean getReadvertise() {
      return _read;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.READVERTISE_ORNO;
   }

}
