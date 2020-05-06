package cc.mschneider.kommpeiler.parser.symboltable;

import java.util.Hashtable;

import cc.mschneider.kommpeiler.parser.productions.Factor;


/**
 * Symbol Table
 * 
 * @author Martin Schneider
 */
public class SymbolTable extends Hashtable<String,Symbol>
{
    private static final long serialVersionUID = 3995465334404915722L;
    
    /**
     * @param key key
     * @param expression expression
     */
    public void update(final String key, final Factor expression)
    {
        if (!containsKey(key))
        {
            Object value = expression.getValue();
            boolean hasValue = false;
            if (value!=null)
            {
                hasValue=true;
            }
            put(key,new Symbol(key,SymbolClass.VAR,null,0,value,hasValue));
        }
        else
        {
            Symbol sym = get(key);
            sym.setValue(expression.getValue());
        }
    }
}
