package batfish.grammar.juniper.system;

public class Sys_HostNameStanza extends SysStanza {
   
	private String _hostName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public Sys_HostNameStanza(String name) {
      _hostName = name;
      set_postProcessTitle("Host Name " + _hostName);
   }
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_hostName() {
      return _hostName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public SysStanzaType getSysType() {
		return SysStanzaType.HOST_NAME;
	}

}
