package io.github.martinschneider.orzo.lexer.tokens;

import java.math.BigDecimal;

public class DoubleNum extends Token implements Num {
  public boolean isFloat;

  public DoubleNum(BigDecimal val, boolean isFloat) {
    super(val);
    this.isFloat = isFloat;
  }

  public DoubleNum(Double val, boolean isFloat) {
    this(BigDecimal.valueOf(val), isFloat);
  }

  public DoubleNum wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder("FP(");
    strBuilder.append(val);
    if (isFloat) {
      strBuilder.append("f");
    }
    strBuilder.append(')');
    return strBuilder.toString();
  }

  @Override
  public void changeSign() {
    val = (Double) val * -1;
  }
}
