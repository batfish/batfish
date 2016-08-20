package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.Ip;

public class BgpGroup implements Serializable {

   public enum BgpGroupType {
      EXTERNAL,
      INTERNAL
   }

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Boolean _advertiseInactive;

   private Boolean _advertisePeerAs;

   private String _description;

   private Boolean _ebgpMultihop;

   private final List<String> _exportPolicies;

   protected String _groupName;

   private final List<String> _importPolicies;

   protected transient boolean _inherited;

   private boolean _ipv6;

   private Ip _localAddress;

   private Integer _localAs;

   private Integer _loops;

   private BgpGroup _parent;

   private Integer _peerAs;

   private Boolean _removePrivate;

   private BgpGroupType _type;

   public BgpGroup() {
      _exportPolicies = new ArrayList<String>();
      _importPolicies = new ArrayList<String>();
   }

   public final void cascadeInheritance() {
      if (_inherited) {
         return;
      }
      _inherited = true;
      if (_parent != null) {
         _parent.cascadeInheritance();
         if (_advertiseInactive == null) {
            _advertiseInactive = _parent._advertiseInactive;
         }
         if (_advertisePeerAs == null) {
            _advertisePeerAs = _parent._advertisePeerAs;
         }
         if (_description == null) {
            _description = _parent._description;
         }
         if (_ebgpMultihop == null) {
            _ebgpMultihop = _parent._ebgpMultihop;
         }
         if (_exportPolicies.size() == 0) {
            _exportPolicies.addAll(_parent._exportPolicies);
         }
         if (_groupName == null) {
            _groupName = _parent._groupName;
         }
         if (_importPolicies.size() == 0) {
            _importPolicies.addAll(_parent._importPolicies);
         }
         if (_localAs == null) {
            _localAs = _parent._localAs;
         }
         if (_loops == null) {
            _loops = _parent._loops;
         }
         if (_localAddress == null) {
            _localAddress = _parent._localAddress;
         }
         if (_peerAs == null) {
            _peerAs = _parent._peerAs;
         }
         if (_type == null) {
            _type = _parent._type;
         }
      }
   }

   public Boolean getAdvertiseInactive() {
      return _advertiseInactive;
   }

   public Boolean getAdvertisePeerAs() {
      return _advertisePeerAs;
   }

   public final String getDescription() {
      return _description;
   }

   public Boolean getEbgpMultihop() {
      return _ebgpMultihop;
   }

   public final List<String> getExportPolicies() {
      return _exportPolicies;
   }

   public String getGroupName() {
      return _groupName;
   }

   public final List<String> getImportPolicies() {
      return _importPolicies;
   }

   public boolean getIpv6() {
      return _ipv6;
   }

   public final Ip getLocalAddress() {
      return _localAddress;
   }

   public final Integer getLocalAs() {
      return _localAs;
   }

   public Integer getLoops() {
      return _loops;
   }

   public final BgpGroup getParent() {
      return _parent;
   }

   public Integer getPeerAs() {
      return _peerAs;
   }

   public Boolean getRemovePrivate() {
      return _removePrivate;
   }

   public final BgpGroupType getType() {
      return _type;
   }

   public void setAdvertiseInactive(boolean advertiseInactive) {
      _advertiseInactive = advertiseInactive;
   }

   public void setAdvertisePeerAs(boolean advertisePeerAs) {
      _advertisePeerAs = advertisePeerAs;
   }

   public final void setDescription(String description) {
      _description = description;
   }

   public void setEbgpMultihop(boolean ebgpMultihop) {
      _ebgpMultihop = ebgpMultihop;
   }

   public void setIpv6(boolean ipv6) {
      _ipv6 = ipv6;
   }

   public final void setLocalAddress(Ip localAddress) {
      _localAddress = localAddress;
   }

   public final void setLocalAs(int localAs) {
      _localAs = localAs;
   }

   public void setLoops(int loops) {
      _loops = loops;
   }

   public final void setParent(BgpGroup parent) {
      _parent = parent;
   }

   public void setPeerAs(int peerAs) {
      _peerAs = peerAs;
   }

   public void setRemovePrivate(boolean removePrivate) {
      _removePrivate = removePrivate;
   }

   public final void setType(BgpGroupType type) {
      _type = type;
   }
}
