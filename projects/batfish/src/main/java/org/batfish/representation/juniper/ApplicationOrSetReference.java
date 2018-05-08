package org.batfish.representation.juniper;

public class ApplicationOrSetReference extends ApplicationSetMemberReference {

  /** */
  private static final long serialVersionUID = 1L;

  public ApplicationOrSetReference(String name) {
    super(name);
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    ApplicationSetMember application = jc.getApplications().get(_name);
    return (application != null) ? application : jc.getApplicationSets().get(_name);
  }
}
