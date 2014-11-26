package batfish.representation.juniper;

public class StaticOptions_Install extends StaticOptions {

   private boolean _install;

   public StaticOptions_Install(boolean b) {
      _install = b;
   }

   public boolean getInstall() {
      return _install;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.INSTALL_ORNO;
   }

}
