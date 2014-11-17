package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;
import edu.buffalo.cse562.visitor.ColumnIndexParser;
import edu.buffalo.cse562.visitor.ExpressionParser;
import edu.buffalo.cse562.visitor.ExpressionProcessor;

public class SelectionOperator implements Operator{

	private Expression exp;
	private Operator input;
	private boolean isDone = false;
	private ArrayList<ColumnDetails> schema;
	private Map<String,Integer> schemaIndex ;
	private ExpressionParser expParser = null;
	private boolean init = false;
	private Expression processedExpression = null;


	public SelectionOperator(Expression exp,Operator input){
		this.exp = exp;
		this.input = input;
	}
	
	public Operator getOperator(){
		return this.input;
	}
	
	public Expression getExpression(){
		return this.exp;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append("Apply ");
		sb.append(exp.toString());
//		sb.append(exp.toString());
		sb.append(input.toString());
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Tuple readTuple() {
		if(!init){
			init = true;
//			System.out.println("Selection" + exp.toString());
		}
		while(!input.isDone()){
			Tuple inputTuple = input.readTuple();
			if(inputTuple != null){
				if(evaluate(inputTuple)){
					return inputTuple;
				}
			}else{
				this.isDone = true;
				break;
			}
		}
//		System.out.println("End of selection" + exp.toString());
		return null;
		
	}

	@Override
	public void reset() {
		input.reset();
		this.isDone = false;
		loadSchema();
		loadExpressionParser();
	}

	private void loadSchema() {
		schema=input.getSchema();
	//	System.out.println("Selection::"+schema);
		ColumnIndexParser sip = new ColumnIndexParser(schema);
		exp.accept(sip);
		//System.out.println("SelectList::"+selectList);
	/*	for(SelectItem selectExpItem : selectList){
			selectExpItem.accept(sip);
		} */
		schemaIndex = sip.getResult();
		//System.out.println(schemaIndex);
		ExpressionProcessor expProcessor = new ExpressionProcessor();
		exp.accept(expProcessor);
		processedExpression = expProcessor.getProcessedExpression();

	}
	
	private void loadExpressionParser(){
		this.expParser = new ExpressionParser(schemaIndex);
	}

	@Override
	public boolean isDone() {
		return this.isDone;
		
	}
	
	public void clear(){
		
	}

	@Override
	public ArrayList<ColumnDetails> getSchema() {
		return schema;
	}
	
	private boolean evaluate(Tuple inputTuple){
		expParser.setInputTuple(inputTuple);
	//	exp.accept(expParser);
		processedExpression.accept(expParser);
		boolean result = expParser.getResult();
		return result;
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
		// TODO Auto-generated method stub
		return schemaIndex;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
}
