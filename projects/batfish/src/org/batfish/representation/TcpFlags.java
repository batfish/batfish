package org.batfish.representation;

public final class TcpFlags {

   public static final int ACK = 0x10;

   public static final int ECN_ECHO = 0x40;

   public static final int FIN = 0x01;

   public static final int PUSH = 0x08;

   public static final int REDUCED = 0x80;

   public static final int RESET = 0x04;

   public static final int SYN = 0x02;

   public static final int URGENT = 0x20;

   private TcpFlags() {
   }
}
