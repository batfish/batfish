package org.batfish.vendor.cisco_aci.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * ACI End Point Group (EPG / fvAEPg) semantic model.
 *
 * <p>An EPG is a collection of endpoints that share similar policy requirements. EPGs are the
 * fundamental building blocks for ACI policy application.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Epg implements Serializable {
  private final String _name;
  private String _tenant;
  private String _applicationProfile;
  private String _bridgeDomain;
  private String _description;
  private List<String> _providedContracts;
  private List<String> _consumedContracts;
  private List<String> _providedContractInterfaces;
  private List<String> _consumedContractInterfaces;
  private List<String> _protectedByTaboos;

  public Epg(String name) {
    _name = name;
    _providedContracts = new ArrayList<>();
    _consumedContracts = new ArrayList<>();
    _providedContractInterfaces = new ArrayList<>();
    _consumedContractInterfaces = new ArrayList<>();
    _protectedByTaboos = new ArrayList<>();
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

  public @Nullable String getApplicationProfile() {
    return _applicationProfile;
  }

  public void setApplicationProfile(String applicationProfile) {
    _applicationProfile = applicationProfile;
  }

  public @Nullable String getBridgeDomain() {
    return _bridgeDomain;
  }

  public void setBridgeDomain(String bridgeDomain) {
    _bridgeDomain = bridgeDomain;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
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
}
