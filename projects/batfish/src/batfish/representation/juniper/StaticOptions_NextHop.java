package batfish.representation.juniper;

public class StaticOptions_NextHop extends StaticOptions {

   private String _ip;

   public StaticOptions_NextHop(String s) {
      _ip = s;
   }

   public String getIp() {
      return _ip;
   }

   @Override
   public StaticOptionsType getType() {
      return StaticOptionsType.NEXT_HOP;
   }

}
