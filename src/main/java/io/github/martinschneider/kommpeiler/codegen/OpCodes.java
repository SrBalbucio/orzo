package io.github.martinschneider.kommpeiler.codegen;

public class OpCodes {
  public static final byte BIPUSH = (byte) 16;
  public static final byte GETSTATIC = (byte) 178;
  public static final byte IADD = (byte) 96;
  public static final byte ICONST_0 = (byte) 3;
  public static final byte ICONST_1 = (byte) 4;
  public static final byte ICONST_2 = (byte) 5;
  public static final byte ICONST_3 = (byte) 6;
  public static final byte ICONST_4 = (byte) 7;
  public static final byte ICONST_5 = (byte) 8;
  public static final byte ICONST_M1 = (byte) 2;
  public static final byte ILOAD = (byte) 21;
  public static final byte ILOAD_0 = (byte) 26;
  public static final byte ILOAD_1 = (byte) 27;
  public static final byte ILOAD_2 = (byte) 28;
  public static final byte ILOAD_3 = (byte) 29;
  public static final byte INVOKEVIRTUAL = (byte) 182;
  public static final byte INVOKESTATIC = (byte) 184;
  public static final byte ISTORE = (byte) 54;
  public static final byte ISTORE_0 = (byte) 59;
  public static final byte ISTORE_1 = (byte) 60;
  public static final byte ISTORE_2 = (byte) 61;
  public static final byte ISTORE_3 = (byte) 62;
  public static final byte ISUB = (byte) 100;
  public static final byte IMUL = (byte) 104;
  public static final byte IDIV = (byte) 108;
  public static final byte IREM = (byte) 112;
  public static final byte LDC = (byte) 18;
  public static final byte IRETURN = (byte) 172;
  public static final byte DRETURN = (byte) 175;
  public static final byte RETURN = (byte) 177;
  public static final byte SIPUSH = (byte) 17;
  public static final byte IF_ICMPEQ = (byte) 159;
  public static final byte IF_ICMPNE = (byte) 160;
  public static final byte IF_ICMPLT = (byte) 161;
  public static final byte IF_ICMPGE = (byte) 162;
  public static final byte IF_ICMPGT = (byte) 163;
  public static final byte IF_ICMPLE = (byte) 164;
  public static final byte IFEQ = (byte) 153;
  public static final byte IFNE = (byte) 154;
  public static final byte IFLT = (byte) 155;
  public static final byte IFGE = (byte) 156;
  public static final byte IFGT = (byte) 157;
  public static final byte IFLE = (byte) 158;
  public static final byte GOTO = (byte) 167;
  public static final byte IINC = (byte) 132;
}
