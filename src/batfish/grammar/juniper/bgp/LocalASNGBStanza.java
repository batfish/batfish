package batfish.grammar.juniper.bgp;

public class LocalASNGBStanza extends NGBStanza {
   private int _localASNum;

   public LocalASNGBStanza(int a) {
      _localASNum = a;
   }

   public int getLocalASNum() {
      return _localASNum;
   }

   @Override
   public NGBType getType() {
      return NGBType.LOCAL_AS;
   }

}
