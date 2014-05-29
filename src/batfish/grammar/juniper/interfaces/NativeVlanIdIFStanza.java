package batfish.grammar.juniper.interfaces;

public class NativeVlanIdIFStanza extends IFStanza {
   private int _nativeVlan;

   public NativeVlanIdIFStanza(int id) {
      _nativeVlan = id;
   }

   public int getNativeVlan() {
      return _nativeVlan;
   }

   @Override
   public IFType getType() {
      return IFType.NATIVE_VLAN_ID;
   }

}
