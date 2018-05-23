package org.batfish.representation.juniper;

public class JunosApplicationReference extends ApplicationSetMemberReference {

  /** */
  private static final long serialVersionUID = 1L;

  public JunosApplicationReference(String name) {
    super(name);
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    return JunosApplication.valueOf(_name);
  }
}
