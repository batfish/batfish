package batfish.grammar.juniper.interfaces;

public class NativeVlanIdFamilyUStanza extends FamilyUStanza {
   private int _nativeVlan;
   
   public NativeVlanIdFamilyUStanza(int id){
      _nativeVlan = id;
   }
   
   public int getNativeVlan(){
      return _nativeVlan;
   }

   @Override
   public FamilyUType getType() {
      return FamilyUType.NATIVE_VLAN;
   }

}
