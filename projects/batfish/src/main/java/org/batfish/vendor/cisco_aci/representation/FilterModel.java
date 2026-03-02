package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * ACI Filter (vzFilter) semantic model.
 *
 * <p>Filters define Layer 2 to Layer 4 traffic classification rules. A filter contains one or more
 * entries that specify match criteria such as Ethernet type, IP protocol, TCP/UDP ports, and ICMP
 * types. Filters are referenced by contract subjects to define allowed traffic patterns between
 * endpoint groups.
 *
 * <p>Named {@code FilterModel} to distinguish from the Jackson POJO {@link AciFilter} (which is
 * used for direct JSON deserialization of the raw ACI API response).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterModel implements Serializable {
  private final String _name;
  private String _tenant;
  private String _description;
  private List<Entry> _entries;

  public FilterModel(String name) {
    _name = name;
    _entries = new ArrayList<>();
  }

  public String getName() {
    return _name;
  }

  public @Nullable String getTenant() {
    return _tenant;
  }

  public void setTenant(String tenant) {
    _tenant = tenant;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public List<Entry> getEntries() {
    return _entries;
  }

  public void setEntries(List<Entry> entries) {
    _entries = new ArrayList<>(entries);
  }

  /** A filter entry (vzEntry) defines specific traffic matching criteria. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Entry implements Serializable {
    private String _name;
    private String _etherType;
    private String _protocol;
    private String _destinationPort;
    private String _sourcePort;
    private String _destinationFromPort;
    private String _destinationToPort;
    private String _sourceFromPort;
    private String _sourceToPort;
    private String _icmpv4Type;
    private String _icmpv4Code;
    private String _icmpv6Type;
    private String _icmpv6Code;
    private String _arpOpcode;
    private Boolean _applyToFragments;
    private Boolean _stateful;
    private String _tcpRules;
    private String _sourceAddress;
    private String _destinationAddress;

    public Entry() {}

    public @Nullable String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public @Nullable String getEtherType() {
      return _etherType;
    }

    public void setEtherType(String etherType) {
      _etherType = etherType;
    }

    public @Nullable String getProtocol() {
      return _protocol;
    }

    public void setProtocol(String protocol) {
      _protocol = protocol;
    }

    public @Nullable String getDestinationPort() {
      return _destinationPort;
    }

    public void setDestinationPort(String destinationPort) {
      _destinationPort = destinationPort;
    }

    public @Nullable String getSourcePort() {
      return _sourcePort;
    }

    public void setSourcePort(String sourcePort) {
      _sourcePort = sourcePort;
    }

    public @Nullable String getDestinationFromPort() {
      return _destinationFromPort;
    }

    public void setDestinationFromPort(String destinationFromPort) {
      _destinationFromPort = destinationFromPort;
    }

    public @Nullable String getDestinationToPort() {
      return _destinationToPort;
    }

    public void setDestinationToPort(String destinationToPort) {
      _destinationToPort = destinationToPort;
    }

    public @Nullable String getSourceFromPort() {
      return _sourceFromPort;
    }

    public void setSourceFromPort(String sourceFromPort) {
      _sourceFromPort = sourceFromPort;
    }

    public @Nullable String getSourceToPort() {
      return _sourceToPort;
    }

    public void setSourceToPort(String sourceToPort) {
      _sourceToPort = sourceToPort;
    }

    public @Nullable String getIcmpv4Type() {
      return _icmpv4Type;
    }

    public void setIcmpv4Type(String icmpv4Type) {
      _icmpv4Type = icmpv4Type;
    }

    public @Nullable String getIcmpv4Code() {
      return _icmpv4Code;
    }

    public void setIcmpv4Code(String icmpv4Code) {
      _icmpv4Code = icmpv4Code;
    }

    public @Nullable String getIcmpv6Type() {
      return _icmpv6Type;
    }

    public void setIcmpv6Type(String icmpv6Type) {
      _icmpv6Type = icmpv6Type;
    }

    public @Nullable String getIcmpv6Code() {
      return _icmpv6Code;
    }

    public void setIcmpv6Code(String icmpv6Code) {
      _icmpv6Code = icmpv6Code;
    }

    public @Nullable String getArpOpcode() {
      return _arpOpcode;
    }

    public void setArpOpcode(String arpOpcode) {
      _arpOpcode = arpOpcode;
    }

    public @Nullable Boolean getApplyToFragments() {
      return _applyToFragments;
    }

    public void setApplyToFragments(Boolean applyToFragments) {
      _applyToFragments = applyToFragments;
    }

    public @Nullable Boolean getStateful() {
      return _stateful;
    }

    public void setStateful(Boolean stateful) {
      _stateful = stateful;
    }

    public @Nullable String getTcpRules() {
      return _tcpRules;
    }

    public void setTcpRules(String tcpRules) {
      _tcpRules = tcpRules;
    }

    public @Nullable String getSourceAddress() {
      return _sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
      _sourceAddress = sourceAddress;
    }

    public @Nullable String getDestinationAddress() {
      return _destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
      _destinationAddress = destinationAddress;
    }
  }
}
