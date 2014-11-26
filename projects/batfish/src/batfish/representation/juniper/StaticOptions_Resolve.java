package batfish.representation.juniper;

public class StaticOptions_Resolve extends StaticOptions {

   private boolean _resolve;

   public StaticOptions_Resolve(boolean b) {
      _resolve = b;
   }

   public boolean getResolve() {
      return _resolve;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.RESOLVE_ORNO;
   }

}
