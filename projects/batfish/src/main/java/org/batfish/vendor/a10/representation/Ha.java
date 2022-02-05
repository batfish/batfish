package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** ACOSv2 {@code ha} (high-availability) configuration. */
public final class Ha implements Serializable {

  public Ha() {
    _checkGateways = ImmutableSet.of();
    _groups = ImmutableMap.of();
  }

  public void addCheckGateway(Ip ip) {
    if (_checkGateways.contains(ip)) {
      return;
    }
    _checkGateways =
        ImmutableSet.<Ip>builderWithExpectedSize(_checkGateways.size() + 1)
            .addAll(_checkGateways)
            .add(ip)
            .build();
  }

  public @Nonnull Set<Ip> getCheckGateways() {
    return _checkGateways;
  }

  public @Nullable Ip getConnMirror() {
    return _connMirror;
  }

  public void setConnMirror(@Nullable Ip connMirror) {
    _connMirror = connMirror;
  }

  public @Nonnull Map<Integer, HaGroup> getGroups() {
    return _groups;
  }

  public @Nonnull HaGroup getOrCreateHaGroup(int id) {
    HaGroup group = _groups.get(id);
    if (group != null) {
      return group;
    }
    group = new HaGroup();
    _groups =
        ImmutableMap.<Integer, HaGroup>builderWithExpectedSize(_groups.size() + 1)
            .putAll(_groups)
            .put(id, group)
            .build();
    return group;
  }

  public @Nullable Integer getId() {
    return _id;
  }

  public void setId(@Nullable Integer id) {
    _id = id;
  }

  public @Nullable Boolean getPreemptionEnable() {
    return _preemptionEnable;
  }

  public void setPreemptionEnable(@Nullable Boolean preemptionEnable) {
    _preemptionEnable = preemptionEnable;
  }

  public @Nullable Integer getSetId() {
    return _setId;
  }

  public void setSetId(@Nullable Integer setId) {
    _setId = setId;
  }

  private @Nonnull Set<Ip> _checkGateways;
  private @Nullable Ip _connMirror;
  private @Nonnull Map<Integer, HaGroup> _groups;
  private @Nullable Integer _id;
  private @Nullable Boolean _preemptionEnable;
  private @Nullable Integer _setId;
}
