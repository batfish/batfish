package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.batfish.representation.Ip;

public class BgpGroup implements Serializable {

   public enum BgpGroupType {
      EXTERNAL,
      INTERNAL
   }

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _description;

   private final List<String> _exportPolicies;

   private final List<String> _importPolicies;

   private transient boolean _inherited;

   private Ip _localAddress;

   private Integer _localAs;

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
         if (_description == null) {
            _description = _parent._description;
         }
         if (_exportPolicies.size() == 0) {
            _exportPolicies.addAll(_parent._exportPolicies);
         }
         if (_importPolicies.size() == 0) {
            _importPolicies.addAll(_parent._importPolicies);
         }
         if (_localAs == null) {
            _localAs = _parent._localAs;
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

   public final String getDescription() {
      return _description;
   }

   public final List<String> getExportPolicies() {
      return _exportPolicies;
   }

   public final List<String> getImportPolicies() {
      return _importPolicies;
   }

   public final Ip getLocalAddress() {
      return _localAddress;
   }

   public final Integer getLocalAs() {
      return _localAs;
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

   public final void setDescription(String description) {
      _description = description;
   }

   public final void setLocalAddress(Ip localAddress) {
      _localAddress = localAddress;
   }

   public final void setLocalAs(int localAs) {
      _localAs = localAs;
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
