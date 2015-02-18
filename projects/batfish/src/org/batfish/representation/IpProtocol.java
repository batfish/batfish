package org.batfish.representation;

import java.util.HashMap;
import java.util.Map;

import org.batfish.main.BatfishException;

public enum IpProtocol {
   AHP(51),
   AN(107),
   Any0HopProtocol(114),
   AnyDistributedFileSystem(68),
   AnyHostInternalProtocol(61),
   AnyLocalNetwork(63),
   AnyPrivateEncryptionScheme(99),
   ARGUS(13),
   ARIS(104),
   AX25(93),
   BBN_RCC_MON(10),
   BNA(49),
   BR_SAT_MON(76),
   CBT(7),
   CFTP(62),
   CHAOS(16),
   CompaqPeer(110),
   CPHB(73),
   CPNX(72),
   CRTP(126),
   CRUDP(127),
   DCCP(33),
   DCN_MEAS(19),
   DDP(37),
   DDX(116),
   DGP(86),
   EGP(8),
   EIGRP(88),
   EMCON(14),
   ENCAP(98),
   ESP(50),
   ETHERIP(97),
   FC(133),
   FIRE(125),
   GGP(3),
   GMTP(100),
   GRE(47),
   HIP(139),
   HMP(20),
   HOPOPT(0),
   I_NLSP(52),
   IATP(117),
   ICMP(1),
   IDPR(35),
   IDPR_CMTP(38),
   IDRP(45),
   IFMP(101),
   IGMP(2),
   IGP(9),
   IL(40),
   IP(256),
   IPComp(108),
   IPCU(71),
   IPINIP(4),
   IPIP(94),
   IPLT(129),
   IPPC(67),
   IPv6(41),
   IPv6_Frag(44),
   IPv6_ICMP(58),
   IPv6_NoNxt(59),
   IPv6_Opts(60),
   IPv6_Route(43),
   IPXinIP(111),
   IRTP(28),
   ISIS(124),
   ISO_IP(80),
   ISO_TP4(29),
   KRYPTOLAN(65),
   L2TP(115),
   LARP(91),
   LEAF1(25),
   LEAF2(26),
   MANAET(138),
   MERIT_INP(32),
   MFE_NSP(31),
   MHRP(48),
   MICP(95),
   MOBILE(55),
   Mobility(135),
   MPLSinIP(137),
   MTP(92),
   MUX(18),
   NARP(54),
   NETBLT(30),
   NSFNET_IGP(85),
   NVPII(11),
   OSPF(89),
   PGM(113),
   PIM(103),
   PIPE(131),
   PNNI(102),
   PRM(21),
   PTP(123),
   PUP(12),
   PVP(75),
   QNX(106),
   RDP(27),
   ROHC(142),
   RSVP(46),
   RSVP_E2E_IGNORE(134),
   RVD(66),
   SAT_EXPAK(64),
   SAT_MON(69),
   SCC_SP(96),
   SCPS(105),
   SCTP(132),
   SDRP(42),
   SECURE_VMTP(82),
   Shim6(140),
   SKIP(57),
   SM(122),
   SMP(121),
   SNP(109),
   Sprite_RPC(90),
   SPS(130),
   SRP(119),
   SSCOPMCE(128),
   ST(5),
   STP(118),
   SUN_ND(77),
   SWIPE(53),
   TCF(87),
   TCP(6),
   THREE_PC(34),
   TLSP(56),
   TPPLUSPLUS(39),
   TRUNK1(23),
   TRUNK2(24),
   TTP(84),
   UDP(17),
   UDPLite(136),
   UTI(120),
   VINES(83),
   VISA(70),
   VMTP(81),
   VRRP(112),
   WB_EXPAK(79),
   WB_MON(78),
   WESP(141),
   WSN(74),
   XNET(15),
   XNS_IDP(22),
   XTP(36);

   private static final Map<Integer, IpProtocol> NUMBER_TO_PROTOCOL_MAP = buildNumberToProtocolMap();

   private static Map<Integer, IpProtocol> buildNumberToProtocolMap() {
      Map<Integer, IpProtocol> map = new HashMap<Integer, IpProtocol>();
      for (IpProtocol protocol : values()) {
         map.put(protocol._number, protocol);
      }
      return map;
   }

   public static IpProtocol fromNumber(int number) {
      IpProtocol ret = NUMBER_TO_PROTOCOL_MAP.get(number);
      if (ret == null) {
         throw new BatfishException("missing enumeration for protocol number: "
               + number);
      }
      return ret;
   }

   private int _number;

   private IpProtocol(int number) {
      _number = number;
   }

   public int number() {
      return _number;
   }

}
