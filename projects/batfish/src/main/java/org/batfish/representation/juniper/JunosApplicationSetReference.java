package org.batfish.representation.juniper;

public class JunosApplicationSetReference extends ApplicationSetMemberReference {

  /** */
  private static final long serialVersionUID = 1L;

  public JunosApplicationSetReference(String name) {
    super(name);
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    return JunosApplicationSet.valueOf(_name).getApplicationSet();
  }
}
