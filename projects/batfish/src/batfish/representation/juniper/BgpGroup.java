package batfish.representation.juniper;

import java.io.Serializable;

import batfish.representation.Ip;

public class BgpGroup implements Serializable {

   public enum BgpGroupType {
      EXTERNAL,
      INTERNAL
   }

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Ip _localAddress;

   private Integer _localAs;

   private String _name;

   private BgpGroup _parent;

   private BgpGroupType _type;

   public Ip getLocalAddress() {
      return _localAddress;
   }

   public Integer getLocalAs() {
      return _localAs;
   }

   public String getName() {
      return _name;
   }

   public BgpGroup getParent() {
      return _parent;
   }

   public BgpGroupType getType() {
      return _type;
   }

   public void setLocalAddress(Ip localAddress) {
      _localAddress = localAddress;
   }

   public void setLocalAs(int localAs) {
      _localAs = localAs;
   }

   public void setParent(BgpGroup parent) {
      _parent = parent;
   }

   public void setType(BgpGroupType type) {
      _type = type;
   }

}
