package org.batfish.datamodel;

import java.io.Serializable;

public final class TcpFlags implements Serializable {

   public static final int ACK = 0x10;

   public static final int CWR = 0x80;

   public static final int ECE = 0x40;

   public static final int FIN = 0x01;

   public static final int PSH = 0x08;

   public static final int RST = 0x04;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public static final int SYN = 0x02;

   public static final int URG = 0x20;

   private boolean _ack;

   private boolean _cwr;

   private boolean _ece;

   private boolean _fin;

   private boolean _psh;

   private boolean _rst;

   private boolean _syn;

   private boolean _urg;

   private boolean _useAck;

   private boolean _useCwr;

   private boolean _useEce;

   private boolean _useFin;

   private boolean _usePsh;

   private boolean _useRst;

   private boolean _useSyn;

   private boolean _useUrg;

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      TcpFlags other = (TcpFlags) obj;
      if (other.toString().equals(this.toString())) {
         return true;
      }
      else {
         return false;
      }
   }

   public boolean getAck() {
      return _ack;
   }

   public boolean getCwr() {
      return _cwr;
   }

   public boolean getEce() {
      return _ece;
   }

   public boolean getFin() {
      return _fin;
   }

   public boolean getPsh() {
      return _psh;
   }

   public boolean getRst() {
      return _rst;
   }

   public boolean getSyn() {
      return _syn;
   }

   public boolean getUrg() {
      return _urg;
   }

   public boolean getUseAck() {
      return _useAck;
   }

   public boolean getUseCwr() {
      return _useCwr;
   }

   public boolean getUseEce() {
      return _useEce;
   }

   public boolean getUseFin() {
      return _useFin;
   }

   public boolean getUsePsh() {
      return _usePsh;
   }

   public boolean getUseRst() {
      return _useRst;
   }

   public boolean getUseSyn() {
      return _useSyn;
   }

   public boolean getUseUrg() {
      return _useUrg;
   }

   public void setAck(boolean ack) {
      _ack = ack;
   }

   public void setCwr(boolean cwr) {
      _cwr = cwr;
   }

   public void setEce(boolean ece) {
      _ece = ece;
   }

   public void setFin(boolean fin) {
      _fin = fin;
   }

   public void setPsh(boolean psh) {
      _psh = psh;
   }

   public void setRst(boolean rst) {
      _rst = rst;
   }

   public void setSyn(boolean syn) {
      _syn = syn;
   }

   public void setUrg(boolean urg) {
      _urg = urg;
   }

   public void setUseAck(boolean useAck) {
      _useAck = useAck;
   }

   public void setUseCwr(boolean useCwr) {
      _useCwr = useCwr;
   }

   public void setUseEce(boolean useEce) {
      _useEce = useEce;
   }

   public void setUseFin(boolean useFin) {
      _useFin = useFin;
   }

   public void setUsePsh(boolean usePsh) {
      _usePsh = usePsh;
   }

   public void setUseRst(boolean useRst) {
      _useRst = useRst;
   }

   public void setUseSyn(boolean useSyn) {
      _useSyn = useSyn;
   }

   public void setUseUrg(boolean useUrg) {
      _useUrg = useUrg;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(toString(_cwr, _useCwr));
      sb.append(toString(_ece, _useEce));
      sb.append(toString(_urg, _useUrg));
      sb.append(toString(_ack, _useAck));
      sb.append(toString(_psh, _usePsh));
      sb.append(toString(_rst, _useRst));
      sb.append(toString(_syn, _useSyn));
      sb.append(toString(_fin, _useFin));
      return sb.toString();
   }

   private String toString(boolean bit, boolean useBit) {
      if (useBit) {
         if (bit) {
            return "1";
         }
         else {
            return "0";
         }
      }
      else {
         return "x";
      }
   }

}
