package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;

public class LimitOperator implements Operator {

	private Operator input = null;
	private long limit;
	private long offset;
	private long counter;
	private boolean isAll;
	private boolean isDone = false;
	private ArrayList<ColumnDetails> schema = null;

	public LimitOperator(Operator input, long limit, long offset, boolean isAll) {
		this.input = input;
		this.limit = limit;
		this.offset = offset;
	}
	
	public Operator getOperator(){
		return this.input;
	}

	@Override
	public Tuple readTuple() {
		if (input == null) {
			return null;
		}

		Tuple tup = null;
		while (counter < offset) {
			tup = input.readTuple();
			counter++;
			if (tup == null) {
				this.isDone=true;
				return null;
			}
		}
		if (!isAll) {
			if (counter < limit + offset) {
				counter++;
				return input.readTuple();
			}
		} else {
			counter++;
			return input.readTuple();
		}
		
		this.isDone=true;
		return null;
	}

	@Override
	public void reset() {
		input.reset();
		schema = input.getSchema();
		counter = 0;
	}

	@Override
	public boolean isDone() {
		return this.isDone;
	}

	@Override
	public void clear() {

	}

	@Override
	public ArrayList<ColumnDetails> getSchema() {
		return schema;
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		sb.append(input);
		sb.append("Limit  ");
		sb.append("from "+offset);
		sb.append("to "+ limit);
		sb.append("]");
		return sb.toString();
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

	public long getLimit(){
		return this.limit;
	}
	
	public long getOffset(){
		return this.offset;
	}
	
	public boolean getIsAll(){
		return this.isAll;
	}
}
