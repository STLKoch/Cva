package cn.misection.cvac.optimize;

import cn.misection.cvac.ast.IVisitor;
import cn.misection.cvac.ast.clas.*;
import cn.misection.cvac.ast.decl.*;
import cn.misection.cvac.ast.entry.*;
import cn.misection.cvac.ast.expr.*;
import cn.misection.cvac.ast.method.*;
import cn.misection.cvac.ast.program.*;
import cn.misection.cvac.ast.statement.*;
import cn.misection.cvac.ast.type.*;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by MI6 root 1/28.
 */
public final class ConstantAndCopyPropagation
        implements IVisitor, Optimizable
{
    /**
     * // constant or copy in current method;
     */
    private Map<String, AbstractExpression> conorcopy;
    private AbstractExpression curExpr;
    private boolean canChange;

    /**
     * // if in while body, the left of assign should be delete from conorcopy
     */
    private boolean inWhile;
    private boolean isOptimizing;

    private boolean isEqual(AbstractExpression fir, AbstractExpression sec)
    {
        return (fir instanceof CvaNumberIntExpr
                && sec instanceof CvaNumberIntExpr
                && ((CvaNumberIntExpr) fir).getValue() == ((CvaNumberIntExpr) sec).getValue())
                || (fir instanceof CvaIdentifierExpr
                && sec instanceof CvaIdentifierExpr
                && ((CvaIdentifierExpr) fir).getLiteral().equals(((CvaIdentifierExpr) sec).getLiteral()));

    }

    private Map<String, AbstractExpression> intersection(
            Map<String, AbstractExpression> first,
            Map<String, AbstractExpression> second)
    {
        Map<String, AbstractExpression> result = new HashMap<>();
        first.forEach((k, v) ->
        {
            if (second.containsKey(k) && isEqual(v, second.get(k)))
            {
                result.put(k, v);
            }
        });
        return result;
    }

    @Override
    public void visit(CvaBooleanType t) {}

    @Override
    public void visit(CvaClassType t) {}

    @Override
    public void visit(CvaIntType t) {}

    @Override
    public void visit(CvaStringType type) {}

    @Override
    public void visit(CvaDeclaration d) {}

    @Override
    public void visit(CvaAddExpr e)
    {
        this.visit(e.getLeft());
        if (this.canChange)
        {
            e.setLeft(this.curExpr);
        }
        this.visit(e.getRight());
        if (this.canChange)
        {
            e.setRight(this.curExpr);
        }
        this.canChange = false;
    }

    @Override
    public void visit(CvaAndAndExpr e)
    {
        this.visit(e.getLeft());
        if (this.canChange)
        {
            e.setLeft(this.curExpr);
        }
        this.visit(e.getRight());
        if (this.canChange)
        {
            e.setRight(this.curExpr);
        }
        this.canChange = false;
    }

    @Override
    public void visit(CvaCallExpr e)
    {
        this.visit(e.getExpr());
        for (int i = 0; i < e.getArgs().size(); i++)
        {
            this.visit(e.getArgs().get(i));
            if (this.canChange)
            {
                e.getArgs().set(i, this.curExpr);
            }
        }
        this.canChange = false;
    }

    @Override
    public void visit(CvaFalseExpr e)
    {
        this.curExpr = e;
        this.canChange = true;
    }

    @Override
    public void visit(CvaIdentifierExpr e)
    {
        if (this.conorcopy.containsKey(e.getLiteral()))
        {
            this.isOptimizing = true;
            this.canChange = true;
            this.curExpr = this.conorcopy.get(e.getLiteral());
        }
        else
        {
            this.canChange = false;
        }
    }

    @Override
    public void visit(CvaLessThanExpr e)
    {
        this.visit(e.getLeft());
        if (this.canChange)
        {
            e.setLeft(this.curExpr);
        }
        this.visit(e.getRight());
        if (this.canChange)
        {
            e.setRight(this.curExpr);
        }
        this.canChange = false;
    }

    @Override
    public void visit(CvaNewExpr e)
    {
        this.canChange = false;
    }

    @Override
    public void visit(CvaNegateExpr e)
    {
        this.visit(e.getExpr());
        if (this.canChange)
        {
            e.setExpr(this.curExpr);
        }
        this.canChange = false;
    }

    @Override
    public void visit(CvaNumberIntExpr e)
    {
        this.curExpr = e;
        this.canChange = true;
    }

    @Override
    public void visit(CvaStringExpr expr)
    {
        // FIXME
    }

    @Override
    public void visit(CvaSubExpr e)
    {
        this.visit(e.getLeft());
        if (this.canChange)
        {
            e.setLeft(this.curExpr);
        }
        this.visit(e.getRight());
        if (this.canChange)
        {
            e.setRight(this.curExpr);
        }
        this.canChange = false;
    }

    @Override
    public void visit(CvaThisExpr e)
    {
        this.canChange = false;
    }

    @Override
    public void visit(CvaMulExpr e)
    {
        this.visit(e.getLeft());
        if (this.canChange)
        {
            e.setLeft(this.curExpr);
        }
        this.visit(e.getRight());
        if (this.canChange)
        {
            e.setRight(this.curExpr);
        }
        this.canChange = false;
    }

    @Override
    public void visit(CvaTrueExpr e)
    {
        this.curExpr = e;
        this.canChange = true;
    }

    @Override
    public void visit(CvaAssignStatement s)
    {
        if (this.inWhile)
        {
            if (this.conorcopy.containsKey(s.getLiteral()))
            {
                this.conorcopy.remove(s.getLiteral());
            }
            return;
        }

        if (s.getExpr() instanceof CvaIdentifierExpr || s.getExpr() instanceof CvaNumberIntExpr)
        {
            this.conorcopy.put(s.getLiteral(), s.getExpr());
        }
        else
        {
            this.visit(s.getExpr());
            if (this.canChange)
            {
                s.setExpr(this.curExpr);
            }
        }
    }

    @Override
    public void visit(CvaBlockStatement s)
    {
        s.getStatementList().forEach(this::visit);
    }

    @Override
    public void visit(CvaIfStatement s)
    {
        if (this.inWhile)
        {
            return;
        }

        this.visit(s.getCondition());
        if (this.canChange)
        {
            s.setCondition(this.curExpr);
        }

        Map<String, AbstractExpression> originalMap = new HashMap<>();
        this.conorcopy.forEach(originalMap::put);
        this.visit(s.getThenStatement());

        Map<String, AbstractExpression> leftMap = this.conorcopy;
        this.conorcopy = originalMap;
        if (s.getElseStatement() != null)
        {
            this.visit(s.getElseStatement());
        }
        this.conorcopy = intersection(leftMap, this.conorcopy);
    }


    @Override
    public void visit(CvaWriteStatement s)
    {
        if (this.inWhile)
        {
            return;
        }

        this.visit(s.getExpr());
        if (this.canChange)
        {
            s.setExpr(curExpr);
        }
    }

    @Override
    public void visit(CvaWhileStatement s)
    {
        // TODO: it is wrong when in multi-layer-loop
        // delete the var which be changed
        this.inWhile = true;
        this.visit(s.getBody());
        this.inWhile = false;

        this.visit(s.getCondition());
        this.visit(s.getBody());
    }

    @Override
    public void visit(CvaMethod cvaMethod)
    {
        this.conorcopy = new HashMap<>();
        cvaMethod.getStatementList().forEach(this::visit);
        this.visit(cvaMethod.getRetExpr());
        if (this.canChange)
        {
            cvaMethod.setRetExpr(this.curExpr);
        }
    }

    @Override
    public void visit(CvaClass cvaClass)
    {
        cvaClass.getMethodList().forEach(m ->
        {
            this.canChange = false;
            this.visit(m);
        });
    }

    @Override
    public void visit(CvaEntry c)
    {
    }

    @Override
    public void visit(CvaProgram p)
    {
        this.isOptimizing = false;
        p.getClassList().forEach(this::visit);
    }

    @Override
    public boolean isOptimizing()
    {
        return isOptimizing;
    }
}
