package edu.buffalo.cse562.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.buffalo.cse562.data.Datum;
import edu.buffalo.cse562.data.ForDate;
import edu.buffalo.cse562.data.ForDouble;
import edu.buffalo.cse562.data.ForLong;
import edu.buffalo.cse562.data.ForString;
import edu.buffalo.cse562.data.Tuple;
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
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

public class AggregateParser implements ExpressionVisitor,SelectItemVisitor{

	private Tuple inputTuple = null;
	private Tuple outputTuple = null;
	private Map<String,Integer> schemaIndex = null;
	private ArrayList<Datum> values;
	private String hashString = null;
	private Map<String,Map<Expression,Set<Datum>>> hashAggregation = new HashMap<String,Map<Expression,Set<Datum>>>();
	//private Object accumulator;
	private boolean valid;
	private Integer index = -1;

	public AggregateParser(Map<String,Integer> schemaIndex){
		this.schemaIndex =schemaIndex;
		values = new ArrayList<Datum>();
	}
	
	public void setTuples(Tuple inputTuple, Tuple outputTuple){
		this.inputTuple = inputTuple;
		this.outputTuple = outputTuple;
		values.clear();
	}
	
	public AggregateParser(Tuple inputTuple,Tuple outputTuple,Map<String,Integer> schemaIndex){
		
//		System.out.println("OutputTuple::"+this.outputTuple.toString());
		this.schemaIndex =schemaIndex;
		values = new ArrayList<Datum>();
	}

	public boolean getResult(){
		return valid;
	}
	
	public Tuple getAggregatedTuple(){
		return outputTuple;
	}
	
	@Override
	public void visit(AllColumns arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllTableColumns arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SelectExpressionItem selExpItem) {
		// TODO Auto-generated method stub
		index = -1;
		Expression exp = selExpItem.getExpression();
	//	System.out.println(exp.toString());

		if(selExpItem.getAlias() != null){
			index = schemaIndex.get(selExpItem.getAlias());
		}else{
			index = schemaIndex.get(exp.toString());
		//	System.out.println(exp.toString());
			//System.out.println(index);
		}
		exp.accept(this);
		//System.out.println(index);
	//	outputTuple.setValue(values.get(values.size() - 1));
	}

	@Override
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(Function function) {
		// TODO Auto-generated method stub
		String functionName = function.getName().toString();
		if(functionName.equals("date")){
			ExpressionList params = function.getParameters();
			List<Expression> expList  = params.getExpressions();
			DateValue dv = new DateValue(expList.get(0).toString());
			dv.accept(this);
		}

		if(functionName.equals("sum")){
//			System.out.println("Sum");
			ExpressionList params = function.getParameters();
			List<Expression> expList  = params.getExpressions();
			for(Expression exp : expList){
				exp.accept(this);
			}
	//		System.out.println("Size::"+outputTuple.getValues().size());
	//		System.out.println(schemaIndex);
	//		System.out.println(outputTuple.getValues());
	//		System.out.println("values::"+values);
			if(outputTuple.getValues().size() <= index){
				outputTuple.addValue(values.get(values.size()-1));
			}else{
				Datum data = outputTuple.getValue(index);
				if(data!=null){
					data = data.add(values.get(values.size() - 1));
					outputTuple.setValue(index,values.get(values.size()-1));
				}else{
					data = values.get(values.size()-1);
				}

				outputTuple.setValue(index,data);
			}			
		}else if(functionName.equals("avg")){
			ExpressionList params = function.getParameters();

			Function sumFunc = new Function();
			sumFunc.setName("sum");
			sumFunc.setParameters(params);
			Datum sum = outputTuple.getValue(schemaIndex.get(sumFunc.toString()));
			Function countFunc = new Function();
			countFunc.setName("count");
			countFunc.setParameters(params);
			Datum count = outputTuple.getValue(schemaIndex.get(countFunc.toString()));
			Datum result = sum.div(count);

			if(outputTuple.getValues().size() <= index){
				outputTuple.addValue(result);
			}else{
				outputTuple.setValue(index,result);
			}
		}else if(functionName.equals("count")){
			if(function.isDistinct()){
				List<Expression> expList = function.getParameters().getExpressions();
				Expression exp = expList.get(0);
				exp.accept(this);
				Datum data = values.get(values.size() - 1);
				if(!hashAggregation.containsKey(hashString)){
					Set<Datum> expressionSet = new HashSet<Datum>();
					Map<Expression,Set<Datum>> expressionMap = new HashMap<Expression, Set<Datum>>();
					expressionMap.put(exp,expressionSet);
					hashAggregation.put(hashString, expressionMap);
				}
				
				Set<Datum> datumSet = hashAggregation.get(hashString).get(exp);
				if(!datumSet.contains(data)){
					datumSet.add(data);
					if(outputTuple.getValues().size() <= index){
						values.add((Datum)new ForLong(1L));
						outputTuple.addValue((Datum)new ForLong(1L));
					}else{
						Datum countData = outputTuple.getValue(index);
						if(countData!=null){
							countData = countData.add((Datum)new ForLong(1L));
						}else{
							countData = (Datum)new ForLong(1L);
						}
						values.add(countData);
						outputTuple.setValue(index,countData);
					}
				}
			}else{
			if(outputTuple.getValues().size() <= index){
				values.add((Datum)new ForLong(1L));
				outputTuple.addValue((Datum)new ForLong(1L));
			}else{
				Datum data = outputTuple.getValue(index);
				if(data!=null){
					data = data.add((Datum)new ForLong(1L));
				}else{
					data = (Datum)new ForLong(1L);
				}
				values.add(data);
				outputTuple.setValue(index,data);
			}
			}

		}else if(functionName.equals("min")){
			ExpressionList params = function.getParameters();
			List<Expression> expList  = params.getExpressions();

			for(Expression exp : expList){
				exp.accept(this);
			}
		}else if(functionName.equals("max")){
			ExpressionList params = function.getParameters();
			List<Expression> expList  = params.getExpressions();

			for(Expression exp : expList){
				exp.accept(this);
			}
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

	public void visit(DoubleValue arg0) {
		// TODO Auto-generated method stub
		
		values.add((new ForDouble(arg0.getValue())));
		
	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub
		values.add((new ForLong(arg0.getValue())));
	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub
		values.add((new ForDate()).getDatum(arg0.getValue().toString()));
		
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
		if(arg0.isNot())
			valid=!getResult();
		else
			valid=getResult();
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer(arg0.toString());
		sb.deleteCharAt(sb.length()-1);
		sb.deleteCharAt(0);
		values.add((new ForString()).getDatum(sb.toString()));		
	}

	@Override
	public void visit(Addition arg0) {
		Expression leftExpression=arg0.getLeftExpression();
    	leftExpression.accept(this);	    
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Datum leftValue = values.get(values.size()-2);
		Datum rightValue = values.get(values.size()-1);
		if(leftValue instanceof ForLong && rightValue instanceof ForDouble){
			values.set(values.size()-2, rightValue.add(leftValue));
		}else{
			values.set(values.size()-2, leftValue.add(rightValue));
		}
		values.remove(values.size()-1);
	}

	@Override
	public void visit(Division arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
//		System.out.println(values);
//		System.out.println(values.get(values.size()-2));
//		System.out.println(values.get(values.size()-1));
		values.set(values.size()-2, values.get(values.size()-2).div(values.get(values.size()-1)));
		values.remove(values.size()-1);
//		System.out.println(values);
	}

	@Override
	public void visit(Multiplication arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		Datum leftValue = values.get(values.size()-2);
		Datum rightValue = values.get(values.size()-1);

		if(leftValue instanceof ForLong && rightValue instanceof ForDouble){
			values.set(values.size()-2, rightValue.mul(leftValue));
		}else{
			values.set(values.size()-2, leftValue.mul(rightValue));
		}		
		values.remove(values.size()-1);
	}

	@Override
	public void visit(Subtraction arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
	
		Datum leftValue = values.get(values.size()-2);
		Datum rightValue = values.get(values.size()-1);
		if(leftValue instanceof ForLong && rightValue instanceof ForDouble){
			values.set(values.size()-2, rightValue.sub(leftValue).mul(new ForDouble((double) -1)));
		}else{
			values.set(values.size()-2, leftValue.sub(rightValue));
		}
		
		values.remove(values.size()-1);
	}

	@Override
	public void visit(AndExpression arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    boolean temp = getResult();
	    if(temp == true){
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = (temp && getResult());
	    }
	}

	@Override
	public void visit(OrExpression arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    boolean temp = getResult();
	    if(temp == false){
	    	Expression rightExpression=arg0.getRightExpression();
	    	rightExpression.accept(this);
	    	valid = (temp || getResult());
	    }
	}

	@Override
	public void visit(Between arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    Expression start = arg0.getBetweenExpressionStart();
	    start.accept(this);
	    Expression end = arg0.getBetweenExpressionEnd();
	    end.accept(this);
	    Datum tupVal = values.get(values.size()-3);
	    boolean temp = (tupVal.greaterThan(values.get(values.size()-2)) || tupVal.isEqualTo(values.get(values.size()-2)));
	    temp = temp && (tupVal.lesserThan(values.get(values.size()-1)) || tupVal.isEqualTo(values.get(values.size()-1)));
	    if(arg0.isNot())
	    	valid = !temp;
	    else
	    	valid = temp;
	    values.remove(values.size()-1);
	    values.remove(values.size()-1);
	    values.remove(values.size()-1);
	}

	@Override
	public void visit(EqualsTo arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = values.get((values.size()-2)).isEqualTo(values.get(values.size()-1));
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(GreaterThan arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = values.get((values.size()-2)).greaterThan(values.get(values.size()-1));
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		int ret = values.get((values.size()-2)).compareTo(values.get(values.size()-1));
		valid = (ret != -1) ? true : false;

		values.remove(values.size()-1);
		values.remove(values.size()-1);
//		System.out.println("GTE::"+valid);
	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
	    if(values.get(values.size()-1)==null)
	    	valid = true;
	    else
	    	valid = false;
	    values.remove(values.size()-1);		
	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinorThan arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = values.get((values.size()-2)).lesserThan(values.get(values.size()-1));
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	//	System.out.println("MT::"+valid);
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		int ret = values.get((values.size()-2)).compareTo(values.get(values.size()-1));
		valid = (ret != 1) ? true : false;
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		Expression leftExpression=arg0.getLeftExpression();
	    leftExpression.accept(this);
		Expression rightExpression=arg0.getRightExpression();
		rightExpression.accept(this);
		valid = !(values.get((values.size()-2)).isEqualTo(values.get(values.size()-1)));
		values.remove(values.size()-1);
		values.remove(values.size()-1);
	}

	@Override
	public void visit(Column column) {		
		if(schemaIndex.containsKey(column.getWholeColumnName())){
			int i = schemaIndex.get(column.getWholeColumnName());
//			System.out.println("ndex::"+i);
			if (outputTuple == null) {
				outputTuple = new Tuple();
			}
		//	outputTuple.setValue(inputTuple.getValue(i));
	//		System.out.println(inputTuple);
			values.add(inputTuple.getValue(i));
	//		System.out.println(column.getColumnName()+"::"+inputTuple.getValue(i));
//			System.out.println(inputTuple.getValue(i));
		}
		// accumulator = tuple.getValue(column.getColumnName());
	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseXor arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setKey(String hashString) {
		// TODO Auto-generated method stub
		this.hashString = hashString;
	}

}
