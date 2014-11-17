package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.data.TupleComparator;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;

public class OrderByOperator implements Operator{

	//private Expression exp;
	private boolean isAsc;
	private String colName;
	private Operator input;
	private int count;
	private boolean isDone = false;
	private ArrayList<ColumnDetails> schema;
	private ArrayList<OrderByElement> orderList = null;
	private ArrayList<Tuple> data;
	
	public OrderByOperator(String colName, boolean order, Operator input){		
		this.input = input;
		this.isAsc = order;
		this.colName = colName;
	}
	
	public OrderByOperator(List<OrderByElement> list, Operator input) {
		this.orderList = (ArrayList<OrderByElement>) list;
		this.input = input;
		// TODO Auto-generated constructor stub
	}

	public boolean getIsAsc(){
		return isAsc;
	}
	
	public String getColumnName(){
		return colName;
	}
	
	public Operator getOperator(){
		return this.input;
	}
	
	public List<OrderByElement> getSortElementList(){
		return this.orderList;
	}
	
	@Override
	public Tuple readTuple() {
		if(isDone)
			return null;
		if(data.size() <= 0){
			return null;
		}
		Tuple tup = data.get(count);
	//	if(isAsc){			
		count++;
		if(count==data.size())
			isDone = true;
		return tup;
	//	}
	/*	else{			
			count--;
			if(count<0)
				isDone = true;
			return tup;
		} */
	}

	@Override
	public void reset() {		
		input.reset();
		loadSchema();		
		initialize();
	//	if(isAsc)
			count=0;
	//	else
	//		count=data.size()-1;
	}

	private void loadSchema() {	
		schema=input.getSchema();
	}

	@Override
	public boolean isDone() {	
		return isDone;
		
	}

	@Override
	public void clear() {
		
	}

	@Override
	public ArrayList<ColumnDetails> getSchema() {
		return schema;
	}
	
	private void initialize(){
		data = new ArrayList<Tuple>();
		while (!input.isDone()){
			Tuple tup = input.readTuple();
			if(tup!=null){
				if(isAsc){
					data.add(tup);
				}
				else{
					data.add(0,tup);
				}
			}else{
				break;
			}
		}
		TupleComparator tupleComparator = new TupleComparator();
		List<Integer> colId = new ArrayList<Integer>();
		List<Boolean> colOrder = new ArrayList<Boolean>();
		for (OrderByElement ele : orderList) {
			String colName = ele.getExpression().toString();
			for (int i = 0; i < schema.size(); i++) {
				Column col = schema.get(i).getColumn();
				if (col != null) {
					String fullname = col.getWholeColumnName();
					String colname = schema.get(i).getColumn().getColumnName();
					if (fullname.equals(colName) || colname.equals(colName)) {
						// tupleComparator.setOnColumn(i);
						colId.add(i);
						colOrder.add(ele.isAsc());
					}
				}
			}
		}
		tupleComparator.setColumnList(colId);
		tupleComparator.setOrder(colOrder);
		Collections.sort(data, tupleComparator);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(input);
		sb.append(" order by");
		sb.append(this.colName);
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}

}
