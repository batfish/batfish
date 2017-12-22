package org.batfish.datamodel;

public enum DscpType {
  AF11(0b001010),
  AF12(0b001100),
  AF13(0b001110),
  AF21(0b010010),
  AF22(0b010100),
  AF23(0b010110),
  AF31(0b011010),
  AF32(0b011100),
  AF33(0b011110),
  AF41(0b100010),
  AF42(0b100100),
  AF43(0b100110),
  CS1(0b001000),
  CS2(0b010000),
  CS3(0b011000),
  CS4(0b100000),
  CS5(0b101000),
  CS6(0b110000),
  CS7(0b111000),
  DEFAULT(0b000000),
  EF(0b101110);

  private final int _num;

  DscpType(int num) {
    _num = num;
  }

  public int number() {
    return _num;
  }
}
