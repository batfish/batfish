package org.batfish.vendor.cisco_aci.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * ACI VPC (Virtual Port Channel) Pair configuration.
 *
 * <p>Represents a vPC domain with two peer switches that appear as a single logical switch to
 * downstream devices.
 */
public class AciVpcPair implements Serializable {

  private String _vpcId;
  private String _vpcName;
  private String _peer1NodeId;
  private String _peer2NodeId;

  public AciVpcPair() {}

  public AciVpcPair(String vpcId, String vpcName, String peer1NodeId, String peer2NodeId) {
    _vpcId = vpcId;
    _vpcName = vpcName;
    _peer1NodeId = peer1NodeId;
    _peer2NodeId = peer2NodeId;
  }

  public @Nullable String getVpcId() {
    return _vpcId;
  }

  public void setVpcId(String vpcId) {
    _vpcId = vpcId;
  }

  public @Nullable String getVpcName() {
    return _vpcName;
  }

  public void setVpcName(String vpcName) {
    _vpcName = vpcName;
  }

  public @Nullable String getPeer1NodeId() {
    return _peer1NodeId;
  }

  public void setPeer1NodeId(String peer1NodeId) {
    _peer1NodeId = peer1NodeId;
  }

  public @Nullable String getPeer2NodeId() {
    return _peer2NodeId;
  }

  public void setPeer2NodeId(String peer2NodeId) {
    _peer2NodeId = peer2NodeId;
  }
}
