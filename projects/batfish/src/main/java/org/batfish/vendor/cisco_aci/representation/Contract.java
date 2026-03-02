package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * ACI Contract (vzBrCP) semantic model.
 *
 * <p>A contract defines the allowed communication between EPGs. It contains subjects and filters
 * that specify the protocols and ports for communication.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contract implements Serializable {
  private final String _name;
  private String _tenant;
  private String _description;
  private List<Subject> _subjects;
  private String _scope;

  public Contract(String name) {
    _name = name;
    _subjects = new ArrayList<>();
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

  public List<Subject> getSubjects() {
    return _subjects;
  }

  public void setSubjects(List<Subject> subjects) {
    _subjects = new ArrayList<>(subjects);
  }

  public @Nullable String getScope() {
    return _scope;
  }

  public void setScope(String scope) {
    _scope = scope;
  }

  /** A contract subject contains filters that define specific traffic rules. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Subject implements Serializable {
    private String _name;
    private List<FilterRef> _filters;

    public Subject() {
      _filters = new ArrayList<>();
    }

    public @Nullable String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public List<FilterRef> getFilters() {
      return _filters;
    }

    public void setFilters(List<FilterRef> filters) {
      _filters = new ArrayList<>(filters);
    }
  }

  /**
   * A contract filter reference (within a subject) defines specific traffic matching criteria.
   *
   * <p>Named FilterRef to distinguish from the top-level {@link FilterModel} (vzFilter semantic
   * model) and the Jackson POJO {@link AciFilter}.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FilterRef implements Serializable {
    private String _name;
    private String _action;
    private String _etherType;
    private String _ipProtocol;
    private List<String> _sourcePorts;
    private List<String> _destinationPorts;
    private String _sourceAddress;
    private String _destinationAddress;
    private String _icmpType;
    private String _icmpCode;
    private String _arpOpcode;
    private Boolean _stateful;

    public FilterRef() {
      _sourcePorts = new ArrayList<>();
      _destinationPorts = new ArrayList<>();
    }

    public @Nullable String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public @Nullable String getAction() {
      return _action;
    }

    public void setAction(String action) {
      _action = action;
    }

    public @Nullable String getEtherType() {
      return _etherType;
    }

    public void setEtherType(String etherType) {
      _etherType = etherType;
    }

    public @Nullable String getIpProtocol() {
      return _ipProtocol;
    }

    public void setIpProtocol(String ipProtocol) {
      _ipProtocol = ipProtocol;
    }

    public List<String> getSourcePorts() {
      return _sourcePorts;
    }

    public void setSourcePorts(List<String> sourcePorts) {
      _sourcePorts = new ArrayList<>(sourcePorts);
    }

    public List<String> getDestinationPorts() {
      return _destinationPorts;
    }

    public void setDestinationPorts(List<String> destinationPorts) {
      _destinationPorts = new ArrayList<>(destinationPorts);
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

    public @Nullable String getIcmpType() {
      return _icmpType;
    }

    public void setIcmpType(String icmpType) {
      _icmpType = icmpType;
    }

    public @Nullable String getIcmpCode() {
      return _icmpCode;
    }

    public void setIcmpCode(String icmpCode) {
      _icmpCode = icmpCode;
    }

    public @Nullable String getArpOpcode() {
      return _arpOpcode;
    }

    public void setArpOpcode(String arpOpcode) {
      _arpOpcode = arpOpcode;
    }

    public @Nullable Boolean getStateful() {
      return _stateful;
    }

    public void setStateful(Boolean stateful) {
      _stateful = stateful;
    }
  }
}
