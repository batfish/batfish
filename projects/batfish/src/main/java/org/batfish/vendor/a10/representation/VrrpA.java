package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Master configuration for vrrp-a. */
public final class VrrpA implements Serializable {

  public VrrpA() {
    _failOverPolicyTemplates = ImmutableMap.of();
    _peerGroup = ImmutableSet.of();
    _vrids = ImmutableMap.of();
  }

  public @Nullable VrrpACommon getCommon() {
    return _common;
  }

  public @Nonnull VrrpACommon getOrCreateCommon() {
    if (_common == null) {
      _common = new VrrpACommon();
    }
    return _common;
  }

  public @Nonnull Map<String, VrrpAFailOverPolicyTemplate> getFailOverPolicyTemplates() {
    return _failOverPolicyTemplates;
  }

  public @Nonnull VrrpAFailOverPolicyTemplate getOrCreateFailOverPolicyTemplate(String name) {
    VrrpAFailOverPolicyTemplate template = _failOverPolicyTemplates.get(name);
    if (template == null) {
      template = new VrrpAFailOverPolicyTemplate();
      _failOverPolicyTemplates =
          ImmutableMap.<String, VrrpAFailOverPolicyTemplate>builderWithExpectedSize(
                  _failOverPolicyTemplates.size() + 1)
              .putAll(_failOverPolicyTemplates)
              .put(name, template)
              .build();
    }
    return template;
  }

  public @Nullable InterfaceReference getInterface() {
    return _interface;
  }

  public void setInterface(@Nullable InterfaceReference iface) {
    _interface = iface;
  }

  public @Nonnull Set<Ip> getPeerGroup() {
    return _peerGroup;
  }

  public void addPeerGroupPeer(Ip ip) {
    if (_peerGroup.contains(ip)) {
      return;
    }
    _peerGroup =
        ImmutableSet.<Ip>builderWithExpectedSize(_peerGroup.size() + 1)
            .addAll(_peerGroup)
            .add(ip)
            .build();
  }

  public @Nonnull Map<Integer, VrrpAVrid> getVrids() {
    return _vrids;
  }

  public @Nonnull VrrpAVrid getOrCreateVrid(int id) {
    VrrpAVrid vrid = _vrids.get(id);
    if (vrid == null) {
      vrid = new VrrpAVrid();
      _vrids =
          ImmutableMap.<Integer, VrrpAVrid>builderWithExpectedSize(_vrids.size() + 1)
              .putAll(_vrids)
              .put(id, vrid)
              .build();
    }
    return vrid;
  }

  public @Nullable String getVridLead() {
    return _vridLead;
  }

  public void setVridLead(@Nullable String vridLead) {
    _vridLead = vridLead;
  }

  private @Nullable VrrpACommon _common;
  private @Nonnull Map<String, VrrpAFailOverPolicyTemplate> _failOverPolicyTemplates;
  private @Nullable InterfaceReference _interface;
  private @Nonnull Set<Ip> _peerGroup;
  private @Nonnull Map<Integer, VrrpAVrid> _vrids;
  private @Nullable String _vridLead;
}
