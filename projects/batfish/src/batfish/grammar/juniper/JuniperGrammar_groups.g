parser grammar JuniperGrammar_groups;

@members {
private List<String> errors = new ArrayList<String>();

public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
	String hdr = getErrorHeader(e);
	String msg = getErrorMessage(e, tokenNames);
	String errorMessage = "JuniperGrammar_groups: " + hdr + " " + msg;
	errors.add(errorMessage);
}

public List<String> getErrors() {
	return errors;
}
}

/* --- Groups Stanza Rules ---------------------------------------------------------------------------*/
groups_stanza returns [JStanza js]
@init {
GroupsStanza gs = new GroupsStanza();
}
  :
  (GROUPS OPEN_BRACE 
  ((x=group_stanza) {gs.addGroup(x);} )+
  CLOSE_BRACE) 
  {js = gs;}
  ;
  
group_stanza returns [GroupStanza gs]
  :
  (name=VARIABLE) {gs = new GroupStanza(name.getText());}
  OPEN_BRACE
  (x=gr_stanza {gs.addSubstanza(x);})+
  CLOSE_BRACE
  ;
     
gr_stanza returns [JStanza grs]
  :
  (x=firewall_stanza 
  |x=null_stanza
  |x=policy_options_stanza
  |x=protocols_stanza
  |x=routing_options_stanza
  |x=interfaces_stanza 
  |x=system_stanza
  ) 
  {grs = x;}
  ;
  
apply_groups_stanza returns [ApplyGroupsStanza ags]//TODO [P0]: DO NOT IGNORE
  :
  (APPLY_GROUPS {ags = new ApplyGroupsStanza(false);}
  |APPLY_GROUPS_EXCEPT {ags = new ApplyGroupsStanza(true);}
  )
  (group_name = VARIABLE {ags.addGroupName(group_name.getText());}
  |group_names = bracketed_list
  {
    for (String g : group_names) {
      {ags.addGroupName(g);}
    }
  }
  )
  SEMICOLON
  ;
    
