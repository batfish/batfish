package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * External EPG (L3ExtEpg) configuration for L3Out.
 *
 * <p>Defines an external endpoint group for external connectivity, including subnets and associated
 * interfaces.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalEpg implements Serializable {
  private final String _name;
  private List<String> _subnets;
  private List<String> _providedContracts;
  private List<String> _consumedContracts;
  private List<String> _providedContractInterfaces;
  private List<String> _consumedContractInterfaces;
  private List<String> _protectedByTaboos;
  private String _nextHop;
  private String _interface;
  private String _description;

  public ExternalEpg(String name) {
    _name = name;
    _subnets = new ArrayList<>();
    _providedContracts = new ArrayList<>();
    _consumedContracts = new ArrayList<>();
    _providedContractInterfaces = new ArrayList<>();
    _consumedContractInterfaces = new ArrayList<>();
    _protectedByTaboos = new ArrayList<>();
  }

  public String getName() {
    return _name;
  }

  public List<String> getSubnets() {
    return _subnets;
  }

  public void setSubnets(List<String> subnets) {
    _subnets = new ArrayList<>(subnets);
  }

  public List<String> getProvidedContracts() {
    return _providedContracts;
  }

  public void setProvidedContracts(List<String> providedContracts) {
    _providedContracts = new ArrayList<>(providedContracts);
  }

  public List<String> getConsumedContracts() {
    return _consumedContracts;
  }

  public void setConsumedContracts(List<String> consumedContracts) {
    _consumedContracts = new ArrayList<>(consumedContracts);
  }

  public List<String> getProvidedContractInterfaces() {
    return _providedContractInterfaces;
  }

  public void setProvidedContractInterfaces(List<String> providedContractInterfaces) {
    _providedContractInterfaces = new ArrayList<>(providedContractInterfaces);
  }

  public List<String> getConsumedContractInterfaces() {
    return _consumedContractInterfaces;
  }

  public void setConsumedContractInterfaces(List<String> consumedContractInterfaces) {
    _consumedContractInterfaces = new ArrayList<>(consumedContractInterfaces);
  }

  public List<String> getProtectedByTaboos() {
    return _protectedByTaboos;
  }

  public void setProtectedByTaboos(List<String> protectedByTaboos) {
    _protectedByTaboos = new ArrayList<>(protectedByTaboos);
  }

  public @Nullable String getNextHop() {
    return _nextHop;
  }

  public void setNextHop(String nextHop) {
    _nextHop = nextHop;
  }

  public @Nullable String getInterface() {
    return _interface;
  }

  public void setInterface(String iface) {
    _interface = iface;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
