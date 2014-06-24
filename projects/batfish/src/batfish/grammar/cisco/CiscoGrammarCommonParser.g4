parser grammar CiscoGrammarCommonParser;

options {
	tokenVocab = CiscoGrammarCommonLexer;
}

access_list_action
:
	PERMIT
	| DENY
;

closing_comment
  :
  COMMENT_CLOSING_LINE
  ;

comment_stanza
  :
  COMMENT_LINE
  ;

community returns [Long l] 
  :
  (
    (part1=DEC COLON part2=DEC) 
                                                  {
                                                   long part1l = Long.parseLong($part1.text);
                                                   long part2l = Long.parseLong($part2.text);
                                                   $l = (part1l << 16) + part2l;
                                                  }
  )
  | (num=DEC 
            {
             $l = Long.parseLong($num.text);
            })
  | (INTERNET 
             {
              $l = 0l;//TODO: change
             })
  | (LOCAL_AS 
             {
              $l = 1l;//TODO: change
             })
  | (NO_ADVERTISE 
                 {
                  $l = 2l;//TODO: change
                 })
  | (NO_EXPORT 
              {
               $l = 3l;//TODO: change
              })
  ;

integer returns [int i]
  :
  (
    x=DEC
    | x=HEX
  )
  
  {
   $i = Integer.parseInt($x.text);
  }
  ;

interface_name returns [String iname]
  :
  name=VARIABLE 
               {
                $iname = $name.text;
               }
  (FORWARD_SLASH x=DEC 
                      {
                       $iname += "/" + $x.text;
                      })?
  ;

port_specifier returns [List < SubRange > portRange = new ArrayList<SubRange>()]
  :
  (EQ (x=port 
             {
              $portRange.add(new SubRange($x.i, $x.i));
             })+)
  | ( (GT x=port) 
                 {
                  $portRange.add(new SubRange($x.i + 1, 65535));
                 })
  | ( (NEQ x=port) 
                  {
                   $portRange.add(new SubRange(0, $x.i - 1));
                   $portRange.add(new SubRange($x.i + 1, 65535));
                  })
  | ( (LT x=port) 
                 {
                  $portRange.add(new SubRange(0, $x.i - 1));
                 })
  | ( (RANGE x=port y=port) 
                           {
                            $portRange.add(new SubRange($x.i, $y.i));
                           })
  ;

port returns [int i]
  :
  (d=DEC 
        {
         $i = $d.int;
        })
  | (BOOTPC 
           {
            $i = 68;
           })
  | (BOOTPS 
           {
            $i = 67;
           })
  | (BGP 
        {
         $i = 179;
        })
  | (CMD 
        {
         $i = 514;
        })
  | (DOMAIN 
           {
            $i = 53;
           })
  | (FTP 
        {
         $i = 21;
        })
  | (FTP_DATA 
             {
              $i = 20;
             })
  | (ISAKMP 
           {
            $i = 500;
           })
  | (LPD 
        {
         $i = 515;
        })
  | (NETBIOS_DGM 
                {
                 $i = 138;
                })
  | (NETBIOS_NS 
               {
                $i = 137;
               })
  | (NETBIOS_SS 
               {
                $i = 139;
               })
  | (NON500_ISAKMP 
                  {
                   $i = 4500;
                  })
  | (NTP 
        {
         $i = 123;
        })
  | (PIM_AUTO_RP 
                {
                 $i = 496;
                })
  | (POP3 
         {
          $i = 110;
         })
  | (SMTP 
         {
          $i = 25;
         })
  | (SNMP 
         {
          $i = 161;
         })
  | (SNMPTRAP 
             {
              $i = 162;
             })
  | (SYSLOG 
           {
            $i = 514;
           })
  | (TACACS 
           {
            $i = 49;
           })
  | (TELNET 
           {
            $i = 23;
           })
  | (TFTP 
         {
          $i = 69;
         })
  | (WWW 
        {
         $i = 80;
        })
  ;

protocol returns [int i]
  :
  (d=DEC 
        {
         $i = $d.int;
        })
  | (ESP 
        {
         $i = 50;
        })
  | (GRE 
        {
         $i = 47;
        })
  | (ICMP 
         {
          $i = 1;
         })
  | (IGMP 
         {
          $i = 2;
         })
  | (IP 
       {
        $i = 0;
       })
  | (OSPF 
         {
          $i = 89;
         })
  | (PIM 
        {
         $i = 103;
        })
  | (SCTP 
         {
          $i = 132;
         })
  | (TCP 
        {
         $i = 6;
        })
  | (UDP 
        {
         $i = 17;
        })
  ;

range returns [List < SubRange > lsr = new ArrayList<SubRange>()]
  :
  (
    (x=subrange 
               {
                $lsr.add($x.s);
               }) ( (COMMA y=subrange) 
                                      {
                                       $lsr.add($y.s);
                                      })*
  )
  | NONE
  ;

subrange returns [SubRange s]
  :
  ( (x=integer DASH y=integer) 
                              {
                               $s = new SubRange($x.i, $y.i);
                              })
  | (x=integer 
              {
               $s = new SubRange($x.i, $x.i);
              })
  ;

