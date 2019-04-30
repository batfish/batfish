package org.batfish.representation.juniper;

public class ApplicationSetReference extends ApplicationSetMemberReference {

  private static final long serialVersionUID = 1L;

  private String _name;

  public ApplicationSetReference(String name) {
    _name = name;
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    return jc.getMasterLogicalSystem().getApplicationSets().get(_name);
  }
}
