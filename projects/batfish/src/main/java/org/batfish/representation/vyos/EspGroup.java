package org.batfish.representation.vyos;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.DiffieHellmanGroup;

public class EspGroup implements Serializable {

  private boolean _compression;

  private int _lifetimeSeconds;

  private final String _name;

  private DiffieHellmanGroup _pfsDhGroup;

  private PfsSource _pfsSource;

  private final Map<Integer, EspProposal> _proposals;

  public EspGroup(String name) {
    _name = name;
    _proposals = new TreeMap<>();
  }

  public boolean getCompression() {
    return _compression;
  }

  public int getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  public String getName() {
    return _name;
  }

  public DiffieHellmanGroup getPfsDhGroup() {
    return _pfsDhGroup;
  }

  public PfsSource getPfsSource() {
    return _pfsSource;
  }

  public Map<Integer, EspProposal> getProposals() {
    return _proposals;
  }

  public void setCompression(boolean compression) {
    _compression = compression;
  }

  public void setLifetimeSeconds(int lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }

  public void setPfsDhGroup(DiffieHellmanGroup dhGroup) {
    _pfsDhGroup = dhGroup;
  }

  public void setPfsSource(PfsSource pfsSource) {
    _pfsSource = pfsSource;
  }
}
