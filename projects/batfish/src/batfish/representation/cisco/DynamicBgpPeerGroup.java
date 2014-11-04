package batfish.representation.cisco;

import batfish.representation.Ip;

public class DynamicBgpPeerGroup extends BgpPeerGroup {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private String _groupName;
   private String _name;
   private Ip _prefix;
   private int _prefixLength;

   public DynamicBgpPeerGroup(Ip prefix, int prefixLength, String name) {
      _name = name;
      _prefix = prefix;
      _prefixLength = prefixLength;
   }

   public String getGroupName() {
      return _groupName;
   }

   @Override
   public String getName() {
      return _name;
   }

   public Ip getPrefix() {
      return _prefix;
   }

   public int getPrefixLength() {
      return _prefixLength;
   }

   public void setGroupName(String groupName) {
      _groupName = groupName;
   }

}
