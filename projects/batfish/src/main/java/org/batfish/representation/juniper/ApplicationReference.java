package org.batfish.representation.juniper;

public class ApplicationReference extends ApplicationSetMemberReference {

  /** */
  private static final long serialVersionUID = 1L;

  private String _name;

  public ApplicationReference(String name) {
    _name = name;
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    return jc.getMasterLogicalSystem().getApplications().get(_name);
  }
}
