package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

public class Cable implements Serializable {

  private SortedMap<String, DocsisPolicy> _docsisPolicies;

  private SortedMap<String, DocsisPolicyRule> _docsisPolicyRules;

  private SortedMap<String, ServiceClass> _serviceClasses;

  private final SortedMap<String, ServiceClass> _serviceClassesByName;

  public Cable() {
    _docsisPolicies = new TreeMap<>();
    _docsisPolicyRules = new TreeMap<>();
    _serviceClasses = new TreeMap<>();
    _serviceClassesByName = new TreeMap<>();
  }

  public SortedMap<String, DocsisPolicy> getDocsisPolicies() {
    return _docsisPolicies;
  }

  public SortedMap<String, DocsisPolicyRule> getDocsisPolicyRules() {
    return _docsisPolicyRules;
  }

  public SortedMap<String, ServiceClass> getServiceClasses() {
    return _serviceClasses;
  }

  @JsonIgnore
  public SortedMap<String, ServiceClass> getServiceClassesByName() {
    return _serviceClassesByName;
  }

  public void setDocsisPolicies(SortedMap<String, DocsisPolicy> docsisPolicies) {
    _docsisPolicies = docsisPolicies;
  }

  public void setDocsisPolicyRules(SortedMap<String, DocsisPolicyRule> docsisPolicyRules) {
    _docsisPolicyRules = docsisPolicyRules;
  }

  public void setServiceClasses(SortedMap<String, ServiceClass> serviceClasses) {
    _serviceClasses = serviceClasses;
  }
}
