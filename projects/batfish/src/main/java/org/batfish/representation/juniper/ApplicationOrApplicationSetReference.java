package org.batfish.representation.juniper;

public class ApplicationOrApplicationSetReference extends ApplicationSetMemberReference {

  /** */
  private static final long serialVersionUID = 1L;

  private String _name;

  public ApplicationOrApplicationSetReference(String name) {
    _name = name;
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    ApplicationSetMember applicationSetMember = jc.getApplications().get(_name);
    return (applicationSetMember != null)
        ? applicationSetMember
        : jc.getApplicationSets().get(_name);
  }
}
