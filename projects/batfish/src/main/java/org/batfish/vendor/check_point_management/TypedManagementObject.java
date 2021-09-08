package org.batfish.vendor.check_point_management;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/** Abstract class representing a management object containing type and name fields. */
@JsonTypeInfo(
    use = Id.NAME,
    visible = true,
    property = "type",
    defaultImpl = UnknownTypedManagementObject.class)
@JsonSubTypes({
  @JsonSubTypes.Type(value = AddressRange.class, name = "address-range"),
  @JsonSubTypes.Type(value = CpmiAnyObject.class, name = "CpmiAnyObject"),
  @JsonSubTypes.Type(value = Global.class, name = "Global"),
  @JsonSubTypes.Type(value = Group.class, name = "group"),
  @JsonSubTypes.Type(value = Host.class, name = "host"),
  @JsonSubTypes.Type(value = Network.class, name = "network"),
  @JsonSubTypes.Type(value = Package.class, name = "package"),
  @JsonSubTypes.Type(value = RulebaseAction.class, name = "RulebaseAction"),
  @JsonSubTypes.Type(value = ServiceGroup.class, name = "service-group"),
  @JsonSubTypes.Type(value = ServiceIcmp.class, name = "service-icmp"),
  @JsonSubTypes.Type(value = ServiceTcp.class, name = "service-tcp"),
  @JsonSubTypes.Type(value = ServiceUdp.class, name = "service-udp"),
})
public abstract class TypedManagementObject extends NamedManagementObject {

  protected TypedManagementObject(String name, Uid uid) {
    super(name, uid);
  }
}
