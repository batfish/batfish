package org.batfish.representation.juniper;

public class ApplicationOrApplicationSetReference extends ApplicationSetMemberReference {

  private String _name;

  public ApplicationOrApplicationSetReference(String name) {
    _name = name;
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    ApplicationSetMember applicationSetMember =
        jc.getMasterLogicalSystem().getApplications().get(_name);
    return (applicationSetMember != null)
        ? applicationSetMember
        : jc.getMasterLogicalSystem().getApplicationSets().get(_name);
  }
}
