package org.batfish.representation.juniper;

import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;

public class JuniperAuthenticationKeyChain extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private final int _definitionLine;

  private String _description;

  private Map<String, JuniperAuthenticationKey> _keys;

  private int _tolerance;

  public JuniperAuthenticationKeyChain(String name, int definitionLine) {
    super(name);
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
