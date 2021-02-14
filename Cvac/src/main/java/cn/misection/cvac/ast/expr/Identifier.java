package cn.misection.cvac.ast.expr;

import cn.misection.cvac.ast.type.AbstractType;

/**
 * @author Military Intelligence 6 root
 * @version 1.0.0
 * @ClassName Identifier
 * @Description TODO
 * @CreateTime 2021年02月14日 19:19:00
 */
public class Identifier extends AbstractExpression
{
    private String literal;

    private AbstractType type;

    /**
     * whether or not a field;
     */
    private boolean fieldFlag;

    public Identifier(int lineNum, String literal)
    {
        super(lineNum);
        this.literal = literal;
    }

    public Identifier(int lineNum, String literal, AbstractType type, boolean fieldFlag)
    {
        super(lineNum);
        this.literal = literal;
        this.type = type;
        this.fieldFlag = fieldFlag;
    }

    public String getLiteral()
    {
        return literal;
    }

    public AbstractType getType()
    {
        return type;
    }

    public boolean isField()
    {
        return fieldFlag;
    }
}
