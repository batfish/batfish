package org.batfish.representation.cisco;

import org.batfish.common.util.DefinedStructure;
import org.batfish.datamodel.IkeProposal;

public class IsakmpPolicy extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private IkeProposal _proposal;

  public IsakmpPolicy(String name, int definitionLine) {
    super(name, definitionLine);
    _proposal = new IkeProposal(name, definitionLine);
  }

  public IkeProposal getProposal() {
    return _proposal;
  }

  public void setProposal(IkeProposal proposal) {
    _proposal = proposal;
  }
}
