package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeHashingAlgorithm;

/** Represents a Cisco FTD IKEv2 policy. */
public class FtdIkev2Policy implements Serializable {

  private final int _priority;
  private final Set<EncryptionAlgorithm> _encryptionAlgorithms;
  private final Set<IkeHashingAlgorithm> _integrityAlgorithms;
  private final Set<IkeHashingAlgorithm> _prfAlgorithms;
  private final Set<DiffieHellmanGroup> _dhGroups;
  private Integer _lifetimeSeconds;

  public FtdIkev2Policy(int priority) {
    _priority = priority;
    _encryptionAlgorithms = new HashSet<>();
    _integrityAlgorithms = new HashSet<>();
    _prfAlgorithms = new HashSet<>();
    _dhGroups = new HashSet<>();
  }

  public int getPriority() {
    return _priority;
  }

  public Set<EncryptionAlgorithm> getEncryptionAlgorithms() {
    return _encryptionAlgorithms;
  }

  public Set<IkeHashingAlgorithm> getIntegrityAlgorithms() {
    return _integrityAlgorithms;
  }

  public Set<IkeHashingAlgorithm> getPrfAlgorithms() {
    return _prfAlgorithms;
  }

  public Set<DiffieHellmanGroup> getDhGroups() {
    return _dhGroups;
  }

  public Integer getLifetimeSeconds() {
    return _lifetimeSeconds;
  }

  public void setLifetimeSeconds(Integer lifetimeSeconds) {
    _lifetimeSeconds = lifetimeSeconds;
  }
}
