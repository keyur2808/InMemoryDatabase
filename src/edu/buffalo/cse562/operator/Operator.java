package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;

public interface Operator {
	public Tuple readTuple();
	public void reset();
	public boolean isDone();
	public void clear();
	public ArrayList<ColumnDetails> getSchema();
	public Map<String,Integer> getSchemaIndex();
	public void accept(OperatorVisitor ov);
}
