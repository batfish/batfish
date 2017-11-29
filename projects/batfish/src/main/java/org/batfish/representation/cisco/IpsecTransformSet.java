package org.batfish.representation.cisco;

import org.batfish.common.util.DefinedStructure;
import org.batfish.datamodel.IpsecProposal;

public class IpsecTransformSet extends DefinedStructure<String> {

  private static final long serialVersionUID = 1L;

  private String _mode;

  private IpsecProposal _proposal;

  public IpsecTransformSet(String name, int definitionLine) {
    super(name, definitionLine);
    _proposal = new IpsecProposal(name, definitionLine);
  }

  public String getMode() {
    return _mode;
  }

  public IpsecProposal getProposal() {
    return _proposal;
  }

  public void setMode(String mode) {
    _mode = mode;
  }
}
