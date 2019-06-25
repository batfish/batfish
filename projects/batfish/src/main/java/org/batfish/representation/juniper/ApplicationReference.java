package org.batfish.representation.juniper;

public class ApplicationReference extends ApplicationSetMemberReference {

  private String _name;

  public ApplicationReference(String name) {
    _name = name;
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    return jc.getMasterLogicalSystem().getApplications().get(_name);
  }
}
