package edu.buffalo.cse562.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import edu.buffalo.cse562.data.DatumExpressionVisitor;
import edu.buffalo.cse562.data.ForDate;
import edu.buffalo.cse562.data.ForDouble;
import edu.buffalo.cse562.data.ForInteger;
import edu.buffalo.cse562.data.ForLong;
import edu.buffalo.cse562.data.ForString;

public class ExpressionProcessor implements ExpressionVisitor,DatumExpressionVisitor {

	Stack<Expression> values = new Stack<Expression>();
	
	public Expression getProcessedExpression(){
		return values.pop();
	}
	@Override 
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Function function) {
		String name = function.getName().toLowerCase();
		if(name.equals("date")){
			ExpressionList expList = function.getParameters();
			List<Expression> list = expList.getExpressions();
			StringBuffer sb = new StringBuffer(list.get(0).toString());
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(0);
			values.push((new ForDate()).getDatum(sb.toString()));
		}
	}	

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue arg0) {
		// TODO Auto-generated method stub
		
		values.push((new ForDouble(arg0.getValue())));
		
	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub
		values.push((new ForLong(arg0.getValue())));
	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub
		values.push((new ForDate()).getDatum(arg0.getValue().toString()));
		
	}

	@Override
	public void visit(TimeValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimestampValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parenthesis arg0) {
		Expression exp = arg0.getExpression();		
		exp.accept(this);
		Parenthesis par = new Parenthesis();
		Expression inExp = values.pop();
		par.setExpression(inExp);
		values.push(par);
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer(arg0.toString());
		sb.deleteCharAt(sb.length()-1);
		sb.deleteCharAt(0);
		values.push((new ForString()).getDatum(sb.toString()));		
	}

	@Override
	public void visit(Addition arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		Addition add = new Addition();
		add.setLeftExpression(left);
		add.setRightExpression(right);
		values.push(add);
	}

	@Override
	public void visit(Division arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		Division div = new Division();
		div.setLeftExpression(left);
		div.setLeftExpression(right);
		values.push(div);
	}

	@Override
	public void visit(Multiplication arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		Multiplication mult = new Multiplication();
		mult.setLeftExpression(left);
		mult.setRightExpression(right);
		values.push(mult);
	}

	@Override
	public void visit(Subtraction arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		Subtraction sub = new Subtraction();
		sub.setLeftExpression(left);
		sub.setRightExpression(right);
		values.push(sub);
		
	}

	@Override
	public void visit(AndExpression arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		AndExpression and = new AndExpression(left, right);
		values.push(and);
	}

	@Override
	public void visit(OrExpression arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		OrExpression or = new OrExpression(left, right);
		values.push(or);
	}

	@Override
	public void visit(Between arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
	    Expression start = arg0.getBetweenExpressionStart();
	    start.accept(this);
	    Expression startExp = values.pop();
	    Expression end = arg0.getBetweenExpressionEnd();
	    end.accept(this);
	    Expression endExp = values.pop();
	    Between btw = new Between();
	    btw.setLeftExpression(left);
	    btw.setBetweenExpressionStart(startExp);
	    btw.setBetweenExpressionEnd(endExp);
	    values.push(btw);
	}

	@Override
	public void visit(EqualsTo arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		EqualsTo eq = new EqualsTo();
		eq.setLeftExpression(left);
		eq.setRightExpression(right);
		values.push(eq);
	}

	@Override
	public void visit(GreaterThan arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		GreaterThan gt = new GreaterThan();
		gt.setLeftExpression(left);
		gt.setRightExpression(right);
		values.push(gt);
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		GreaterThanEquals gte = new GreaterThanEquals();
		gte.setLeftExpression(left);
		gte.setRightExpression(right);
		values.push(gte);
	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void visit(MinorThan arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		MinorThan mt = new MinorThan();
		mt.setLeftExpression(left);
		mt.setRightExpression(right);
		values.push(mt);
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		MinorThanEquals mte = new MinorThanEquals();
		mte.setLeftExpression(left);
		mte.setRightExpression(right);
		values.push(mte);
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		NotEqualsTo net = new NotEqualsTo();
		net.setLeftExpression(left);
		net.setRightExpression(right);
		values.push(net);
	}

	@Override
	public void visit(Column column) {	
		values.add(column);
	}

	@Override
	public void visit(SubSelect arg0) {
		
	}

	@Override
	public void visit(CaseExpression arg0) {		
	}

	@Override
	public void visit(WhenClause arg0) {		
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		Concat concat = new Concat();
		concat.setLeftExpression(left);
		concat.setRightExpression(right);
		values.push(concat);
	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd arg0) {		
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		BitwiseAnd ba = new BitwiseAnd();
		ba.setLeftExpression(left);
		ba.setRightExpression(right);
		values.push(ba);
	}

	@Override
	public void visit(BitwiseOr arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		BitwiseOr bo = new BitwiseOr();
		bo.setLeftExpression(left);
		bo.setRightExpression(right);
		values.push(bo);

	}

	@Override
	public void visit(BitwiseXor arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression left = values.pop();
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Expression right = values.pop();
		BitwiseXor bx = new BitwiseXor();
		bx.setLeftExpression(left);
		bx.setRightExpression(right);
		values.push(bx);
	}
	
	@Override
	public void visit(ForDate arg0) {
	}

	@Override
	public void visit(ForString arg0) {
	}

	@Override
	public void visit(ForLong arg0) {
	}

	@Override
	public void visit(ForInteger arg0) {
	}

	@Override
	public void visit(ForDouble arg0) {
	}
}
