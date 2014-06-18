package batfish.grammar.cisco.ospf;

public class NullIPv6ROStanza extends IPv6ROStanza {

   @Override
   public IPv6ROStanzaType getType() {
      return IPv6ROStanzaType.NULL;
   }

}
