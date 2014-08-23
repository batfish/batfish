package batfish.collections;

public class HostnameAclNamePair extends Pair<String, String> {

   private static final long serialVersionUID = 1L;

   public HostnameAclNamePair(String hostname, String aclName) {
      super(hostname, aclName);
   }

}
