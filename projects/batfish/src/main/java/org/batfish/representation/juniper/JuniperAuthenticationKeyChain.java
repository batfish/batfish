package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class JuniperAuthenticationKeyChain implements Serializable {

  private final int _definitionLine;

  private String _description;

  private Map<String, JuniperAuthenticationKey> _keys;

  private final String _name;

  private int _tolerance;

  public JuniperAuthenticationKeyChain(String name, int definitionLine) {
    _name = name;
    _definitionLine = definitionLine;
    _keys = new TreeMap<>();
  }

  public int getDefinitionLine() {
    return _definitionLine;
  }

  public String getDescription() {
    return _description;
  }

  public Map<String, JuniperAuthenticationKey> getKeys() {
    return _keys;
  }

  public String getName() {
    return _name;
  }

  public int getTolerance() {
    return _tolerance;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setKeys(Map<String, JuniperAuthenticationKey> keys) {
    _keys = keys;
  }

  public void setTolerance(int tolerance) {
    _tolerance = tolerance;
  }
}
