package org.batfish.common.topology.broadcast;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;

/** Models {@link DeviceBroadcastDomain} and {@link PhysicalInterface} connections */
public interface DomainToL2Interface {
  /**
   * Returns the VLAN that a frame with the given tag will be modeled as, or {@link
   * Optional#empty()} if the frame will not be processed.
   */
  Optional<Integer> receiveTag(EthernetTag tag);

  /**
   * Returns the {@link EthernetTag} that data in the given VLAN will be sent as, or {@link
   * Optional#empty()} if data will not be sent out the interface.
   */
  Optional<EthernetTag> sendFromVlan(int vlan);

  /**
   * A switchport in access mode will only accept untagged frames, and will send frames from the
   * specific vlan without a tag.
   */
  class AccessMode implements DomainToL2Interface {
    public AccessMode(int vlan) {
      _vlan = vlan;
    }

    @Override
    public Optional<Integer> receiveTag(EthernetTag tag) {
      if (!tag.hasTag()) {
        // Untagged -> the access vlan.
        return Optional.of(_vlan);
      }
      return Optional.empty();
    }

    @Override
    public Optional<EthernetTag> sendFromVlan(int vlan) {
      if (vlan != _vlan) {
        // Not in this vlan.
        return Optional.empty();
      }
      // Send untagged frames.
      return Optional.of(EthernetTag.untagged());
    }

    private final int _vlan;
  }

  /**
   * A switchport in trunk mode will accept untagged frames in the native vlan (if present), will
   * accept tagged frames in any allowed vlan. The reverse is true: native vlan will be sent
   * untagged, all tagged frames in an allowed vlan will also be accepted.
   */
  class Trunk implements DomainToL2Interface {
    public Trunk(IntegerSpace allowedVlans, @Nullable Integer nativeVlanId) {
      _allowedVlans = allowedVlans;
      _nativeVlanId = nativeVlanId;
    }

    @Override
    public Optional<Integer> receiveTag(EthernetTag tag) {
      if (tag.hasTag() && Objects.equals(tag.getTag(), _nativeVlanId)) {
        // Trunks reject frames tagged with native VLAN.
        return Optional.empty();
      }

      // Present if 1) tag is present and allowed, or 2) no tag, native vlan is allowed.
      Integer effectiveVlan = tag.hasTag() ? (Integer) tag.getTag() : _nativeVlanId;
      return Optional.ofNullable(effectiveVlan).filter(_allowedVlans::contains);
    }

    @Override
    public Optional<EthernetTag> sendFromVlan(int vlan) {
      if (!_allowedVlans.contains(vlan)) {
        return Optional.empty();
      }
      if (_nativeVlanId != null && _nativeVlanId == vlan) {
        return Optional.of(EthernetTag.untagged());
      }
      return Optional.of(EthernetTag.tagged(vlan));
    }

    private final @Nullable Integer _nativeVlanId;
    private final IntegerSpace _allowedVlans;
  }
}
