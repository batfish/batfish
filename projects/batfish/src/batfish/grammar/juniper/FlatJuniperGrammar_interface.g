parser grammar FlatJuniperGrammar_interface;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "FlatJuniperGrammar_interface: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

address_family_u_stanza returns [FamilyUStanza fus]
  :
  ADDRESS
  (
    x=IP_ADDRESS_WITH_MASK
    | x=IPV6_ADDRESS_WITH_MASK
  )
  
  {
   fus = new AddressFamilyUStanza(x.getText());
  }
  NEWLINE
  ;

description_if_stanza
  :
  DESCRIPTION ~NEWLINE+ NEWLINE
  ;

disable_if_stanza returns [IFStanza ifs]
  :
  (DISABLE NEWLINE) 
                   {
                    ifs = new DisableIFStanza();
                   }
  ;

family_u_stanza returns [FamilyUStanza fus]
  :
  (
    x=address_family_u_stanza
    | x=interface_mode_family_u_stanza
    | x=null_family_u_stanza
    | x=vlan_id_family_u_stanza
    | x=vlan_id_list_family_u_stanza
  )
  
  {
   fus = x;
  }
  ;

family_unit_if_stanza returns [FlatFamilyUIFStanza fuifs]
@init {
FamilyType ft = null;
}
  :
  FAMILY
  (
    (INET) 
          {
           ft = FamilyType.INET;
          }
    | (INET6) 
             {
              ft = FamilyType.INET6;
             }
    | (BRIDGE) 
              {
               ft = FamilyType.BRIDGE;
              }
  )
  
  {
   fuifs = new FlatFamilyUIFStanza(ft);
  }
  (
    (x=family_u_stanza) 
                       {
                        fuifs.processFamilyUStanza(x);
                       }
    | NEWLINE
  )
  ;

filter_family_u_stanza
  :
  FILTER ~NEWLINE+ NEWLINE
  ;

if_stanza returns [IFStanza ifs]
  :
  (
    x=disable_if_stanza
    | x=null_if_stanza
    | x=unit_if_stanza
  )
  
  {
   ifs = x;
  }
  ;

interface_mode_family_u_stanza returns [FamilyUStanza fus]
@init {
SwitchportMode mode = null;
}
  :
  INTERFACE_MODE
  (
    (TRUNK) 
           {
            mode = SwitchportMode.TRUNK;
           }
    | (ACCESS) 
              {
               mode = SwitchportMode.ACCESS;
              }
  )
  
  {
   fus = new InterfaceModeFamilyUStanza(mode);
  }
  NEWLINE
  ;

interfaces_stanza returns [JStanza js]
  :
  (INTERFACES name=VARIABLE l=if_stanza) 
                                        {
                                         FlatInterfacesStanza is = new FlatInterfacesStanza(name.getText());
                                         is.processStanza(l);
                                         js = is;
                                        }
  ;

mtu_if_stanza
  :
  MTU ~NEWLINE+ NEWLINE
  ;

no_redirects_family_u_stanza
  :
  NO_REDIRECTS NEWLINE
  ;

null_family_u_stanza returns [FamilyUStanza fus = new NullFamilyUStanza()]
  :
  filter_family_u_stanza
  | mtu_if_stanza
  | no_redirects_family_u_stanza
  ;

null_if_stanza returns [IFStanza ifs=new NullIFStanza()]
  :
  description_if_stanza
  | mtu_if_stanza
  | traps_if_stanza
  ;

traps_if_stanza
  :
  TRAPS NEWLINE
  ;

unit_if_stanza returns [IFStanza ifs]
@init {
FlatUnitIFStanza uifs;
}
  :
  (
    UNIT num=integer 
                    {
                     uifs = new FlatUnitIFStanza(num);
                    }
    (
      description_if_stanza
      | (x=family_unit_if_stanza) 
                                 {
                                  uifs.processFamily(x);
                                 }
    )
    
    {
     ifs = uifs;
    }
  )
  ;

vlan_id_family_u_stanza returns [FamilyUStanza fus]
  :
  (VLAN_ID num=integer NEWLINE) 
                               {
                                fus = new VlanIdFamilyUStanza(num);
                               }
  ;

vlan_id_list_family_u_stanza returns [FamilyUStanza fus]
@init {
int end;
VlanIdListFamilyUStanza vfus = new VlanIdListFamilyUStanza();
}
  :
  (
    VLAN_ID_LIST OPEN_BRACKET
    (
      (
        (stnum=integer) 
                       {
                        end = stnum;
                       }
        ( (DASH ednum=integer) 
                              {
                               end = ednum;
                              })?
      )
      
      {
       vfus.addPair(stnum, end);
      }
    )+
    CLOSE_BRACKET NEWLINE
  )
  
  {
   fus = vfus;
  }
  ;
