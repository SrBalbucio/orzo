package io.github.martinschneider.orzo.lexer.tokens;

import java.math.BigInteger;

public class IntNum extends Token implements Num {
  public boolean isLong;

  public IntNum(BigInteger val, boolean isLong) {
    super(val);
    this.isLong = isLong;
  }

  public IntNum(Integer val, boolean isLong) {
    this(BigInteger.valueOf(val), isLong);
  }

  public IntNum wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  public long intValue() {
    return ((BigInteger) val).longValue();
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder("INT(");
    strBuilder.append(val);
    if (isLong) {
      strBuilder.append("l");
    }
    strBuilder.append(')');
    return strBuilder.toString();
  }

  @Override
  public void changeSign() {
    val = ((BigInteger) val).multiply(BigInteger.valueOf(-1));
  }
}
