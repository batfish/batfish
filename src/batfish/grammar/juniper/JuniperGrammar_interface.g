parser grammar JuniperGrammar_interface;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_interface: " + hdr + " " + msg;
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
  (
    (SEMICOLON)
    | (OPEN_BRACE substanza+ CLOSE_BRACE)
  )
  
  {
   fus = new AddressFamilyUStanza(x.getText());
  }
  ;

aggregated_ether_options_if_stanza
  :
  AGGREGATED_ETHER_OPTIONS OPEN_BRACE substanza+ CLOSE_BRACE
  ;

description_if_stanza
  :
  DESCRIPTION ~SEMICOLON* SEMICOLON
  ;

disable_if_stanza returns [IFStanza ifs]
  :
  (DISABLE SEMICOLON) 
                     {
                      ifs = new DisableIFStanza();
                     }
  ;

enable_if_stanza
  :
  ENABLE SEMICOLON
  ;

family_u_stanza returns [FamilyUStanza fus]
  :
  (
    x=address_family_u_stanza
    | x=filter_family_u_stanza
    | x=interface_mode_family_u_stanza
    | x=native_vlan_id_family_u_stanza
    | x=null_family_u_stanza
    | x=vlan_id_family_u_stanza
    | x=vlan_id_list_family_u_stanza
    | x=vlan_members_family_u_stanza
  )
  
  {
   fus = x;
  }
  ;

family_u_stanza_list returns [List<FamilyUStanza> fl = new ArrayList<FamilyUStanza>()]
  :
  ( (x=family_u_stanza) 
                       {
                        fl.add(x);
                       })+
  ;

family_unit_if_stanza returns [FamilyUIFStanza fuifs]
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
    |
    (
      BRIDGE
      | ETHERNET_SWITCHING
    )
    
    {
     ft = FamilyType.BRIDGE;
    }
  )
  
  {
   fuifs = new FamilyUIFStanza(ft);
  }
  (
    (OPEN_BRACE x=family_u_stanza_list CLOSE_BRACE) 
                                                   {
                                                    for (FamilyUStanza fus : x) {
                                                    	fuifs.processFamilyUStanza(fus);
                                                    }
                                                   }
    | SEMICOLON
  )
  ;

filter_family_u_stanza returns [FamilyUStanza fus]
@init {
FilterFamilyUStanza ffus = new FilterFamilyUStanza();
}
  :
  (
    FILTER OPEN_BRACE
    (
      (INPUT x=VARIABLE SEMICOLON) 
                                  {
                                   ffus.addFilter(x.getText(), true);
                                  }
      | (OUTPUT x=VARIABLE SEMICOLON) 
                                    {
                                     ffus.addFilter(x.getText(), false);
                                    }
    )+
    CLOSE_BRACE
  )
  
  {
   fus = ffus;
  }
  ;

gigether_options_if_stanza
  :
  GIGETHER_OPTIONS OPEN_BRACE substanza+ CLOSE_BRACE
  ;

if_stanza returns [IFStanza ifs]
  :
  (
    x=disable_if_stanza
    | x=native_vlan_id_if_stanza
    | x=null_if_stanza
    | x=unit_if_stanza
  )
  
  {
   ifs = x;
  }
  ;

if_stanza_list returns [List<IFStanza> ifl = new ArrayList<IFStanza>()]
  :
  ( (x=if_stanza) 
                 {
                  ifl.add(x);
                 })+
  ;

interface_mode_family_u_stanza returns [FamilyUStanza fus]
@init {
SwitchportMode mode = SwitchportMode.ACCESS;
}
  :
  (
    INTERFACE_MODE
    | PORT_MODE
  )
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
  SEMICOLON
  ;

interface_stanza returns [InterfaceStanza ifs]
  :
  (
    (
      (name=VARIABLE)
      | (name=VLAN)
    )
    OPEN_BRACE ifl=if_stanza_list CLOSE_BRACE
  )
  
  {
   ifs = new InterfaceStanza(name.getText());
   for (IFStanza iif : ifl) {
   	ifs.processStanza(iif);
   }
  }
  ;

interface_stanza_list returns [List<InterfaceStanza> l = new ArrayList<InterfaceStanza>()]
  :
  ( ( (x=interface_stanza) 
                          {
                           l.add(x);
                          }))+
  ;

interfaces_stanza returns [JStanza js]
  :
  (INTERFACES OPEN_BRACE l=interface_stanza_list CLOSE_BRACE) 
                                                             {
                                                              InterfacesStanza is = new InterfacesStanza();
                                                              for (InterfaceStanza x : l) {
                                                              	is.processStanza(x);
                                                              }
                                                              js = is;
                                                             }
  ;

mtu_if_stanza
  :
  MTU ~SEMICOLON* SEMICOLON
  ;

native_vlan_id_family_u_stanza returns [FamilyUStanza fus]
  :
  (NATIVE_VLAN_ID id=integer SEMICOLON) 
                                       {
                                        fus = new NativeVlanIdFamilyUStanza(id);
                                       }
  ;

native_vlan_id_if_stanza returns [IFStanza ifs]
  :
  (NATIVE_VLAN_ID id=integer SEMICOLON) 
                                       {
                                        ifs = new NativeVlanIdIFStanza(id);
                                       }
  ;

no_redirects_family_u_stanza
  :
  NO_REDIRECTS SEMICOLON
  ;

null_family_u_stanza returns [FamilyUStanza fus = new NullFamilyUStanza()]
  :
  mtu_if_stanza
  | no_redirects_family_u_stanza
  | primary_family_u_stanza
  | rpf_check_family_u_stanza
  | sampling_family_u_stanza
  ;

null_if_stanza returns [IFStanza ifs=new NullIFStanza()]
  :
  aggregated_ether_options_if_stanza
  | enable_if_stanza
  | description_if_stanza
  | gigether_options_if_stanza
  | mtu_if_stanza
  | traps_if_stanza
  | vlan_tagging_if_stanza
  ;

primary_family_u_stanza
  :
  PRIMARY SEMICOLON
  ;

rpf_check_family_u_stanza
  :
  RPF_CHECK OPEN_BRACE substanza+ CLOSE_BRACE
  ;

sampling_family_u_stanza
  :
  SAMPLING OPEN_BRACE substanza+ CLOSE_BRACE
  ;

traps_if_stanza
  :
  TRAPS SEMICOLON
  ;

unit_if_stanza returns [IFStanza ifs]
@init {
UnitIFStanza uifs;
}
  :
  (
    UNIT num=integer 
                    {
                     uifs = new UnitIFStanza(num);
                    }
    OPEN_BRACE
    (
      description_if_stanza
      | (DISABLE SEMICOLON) 
                           {
                            uifs.setDisable();
                           }
      | (ARP_RESP SEMICOLON)
      | (VLAN_ID num=integer SEMICOLON) 
                                       {
                                        uifs.setAccessVlan(num);
                                       }
      | (x=family_unit_if_stanza) 
                                 {
                                  uifs.processFamily(x);
                                 }
    )+
    CLOSE_BRACE 
               {
                ifs = uifs;
               }
  )
  ;

vlan_id_family_u_stanza returns [FamilyUStanza fus]
  :
  (VLAN_ID num=integer SEMICOLON) 
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
    CLOSE_BRACKET SEMICOLON
  )
  
  {
   fus = vfus;
  }
  ;

vlan_members_family_u_stanza returns [FamilyUStanza fus]
@init {
int end;
VlanIdListFamilyUStanza vfus = new VlanIdListFamilyUStanza();
}
  :
  (
    VLAN OPEN_BRACE MEMBERS
    (
      (
        OPEN_BRACKET
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
        CLOSE_BRACKET
      )
      |
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
      )
    )
    SEMICOLON CLOSE_BRACE
  )
  
  {
   fus = vfus;
  }
  ;

vlan_tagging_if_stanza
  :
  VLAN_TAGGING SEMICOLON
  ;
