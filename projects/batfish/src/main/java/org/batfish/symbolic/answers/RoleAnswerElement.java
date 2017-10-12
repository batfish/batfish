package org.batfish.symbolic.answers;

import java.util.List;
import java.util.SortedSet;
import org.batfish.datamodel.answers.AnswerElement;

public class RoleAnswerElement implements AnswerElement {

  private List<SortedSet<String>> _importBgpEcs;

  private List<SortedSet<String>> _exportBgpEcs;

  private List<SortedSet<String>> _incomingAclEcs;

  private List<SortedSet<String>> _outgoingAclEcs;

  private List<SortedSet<String>> _interfaceEcs;

  private List<SortedSet<String>> _nodeEcs;

  public List<SortedSet<String>> getImportBgpEcs() {
    return _importBgpEcs;
  }

  public List<SortedSet<String>> getExportBgpEcs() {
    return _exportBgpEcs;
  }

  public List<SortedSet<String>> getIncomingAclEcs() {
    return _incomingAclEcs;
  }

  public List<SortedSet<String>> getOutgoingAclEcs() {
    return _outgoingAclEcs;
  }

  public List<SortedSet<String>> getInterfaceEcs() {
    return _interfaceEcs;
  }

  public List<SortedSet<String>> getNodeEcs() {
    return _nodeEcs;
  }

  public void setImportBgpEcs(List<SortedSet<String>> x) {
    this._importBgpEcs = x;
  }

  public void setExportBgpEcs(List<SortedSet<String>> x) {
    this._exportBgpEcs = x;
  }

  public void setIncomingAclEcs(List<SortedSet<String>> x) {
    this._incomingAclEcs = x;
  }

  public void setOutgoingAclEcs(List<SortedSet<String>> x) {
    this._outgoingAclEcs = x;
  }

  public void setInterfaceEcs(List<SortedSet<String>> x) {
    this._interfaceEcs = x;
  }

  public void setNodeEcs(List<SortedSet<String>> x) {
    this._nodeEcs = x;
  }

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();

    if (_importBgpEcs != null) {
      sb.append("BGP Import ECs:\n");
      for (SortedSet<String> ec : _importBgpEcs) {
        sb.append("  Class:\n");
        for (String s : ec) {
          sb.append("    ").append(s).append("\n");
        }
      }
    }

    if (_exportBgpEcs != null) {
      sb.append("BGP Export ECs:\n");
      for (SortedSet<String> ec : _exportBgpEcs) {
        sb.append("  Class:\n");
        for (String s : ec) {
          sb.append("    ").append(s).append("\n");
        }
      }
    }

    if (_incomingAclEcs != null) {
      sb.append("Incoming ACL ECs:\n");
      for (SortedSet<String> ec : _incomingAclEcs) {
        sb.append("  Class:\n");
        for (String s : ec) {
          sb.append("    ").append(s).append("\n");
        }
      }
    }

    if (_outgoingAclEcs != null) {
      sb.append("Outgoing ACL ECs:\n");
      for (SortedSet<String> ec : _outgoingAclEcs) {
        sb.append("  Class:\n");
        for (String s : ec) {
          sb.append("    ").append(s).append("\n");
        }
      }
    }

    if (_interfaceEcs != null) {
      sb.append("Interface ECs:\n");
      for (SortedSet<String> ec : _interfaceEcs) {
        sb.append("  Class:\n");
        for (String s : ec) {
          sb.append("    ").append(s).append("\n");
        }
      }
    }

    if (_nodeEcs != null) {
      sb.append("Node ECs:\n");
      for (SortedSet<String> ec : _nodeEcs) {
        sb.append("  Class:\n");
        for (String s : ec) {
          sb.append("    ").append(s).append("\n");
        }
      }
    }

    return sb.toString();
  }
}
