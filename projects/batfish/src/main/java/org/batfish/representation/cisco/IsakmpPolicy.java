package org.batfish.representation.cisco;

import org.batfish.common.util.ComparableStructure;
import org.batfish.common.util.DefinedStructure;
import org.batfish.datamodel.IkeProposal;

public class IsakmpPolicy extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private IkeProposal _proposal;

  public IsakmpPolicy(String name) {
    super(name);
    _proposal = new IkeProposal(name, DefinedStructure.IGNORED_DEFINITION_LINE);
  }

  public IkeProposal getProposal() {
    return _proposal;
  }

  public void setProposal(IkeProposal proposal) {
    _proposal = proposal;
  }
}
