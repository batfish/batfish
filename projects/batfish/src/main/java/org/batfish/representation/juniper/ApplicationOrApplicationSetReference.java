package org.batfish.representation.juniper;

public class ApplicationOrApplicationSetReference extends ApplicationSetMemberReference {

  /** */
  private static final long serialVersionUID = 1L;

  public ApplicationOrApplicationSetReference(String name) {
    super(name);
  }

  public ApplicationOrApplicationSetReference(Application application) {
    super(application);
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    if (_application != null) {
      return _application;
    }
    ApplicationSetMember application = jc.getApplications().get(_name);
    return (application != null) ? application : jc.getApplicationSets().get(_name);
  }
}
