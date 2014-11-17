package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Datum;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;
import edu.buffalo.cse562.visitor.ColumnIndexParser;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class MergeJoin extends JoinOperator{

	private EqualsTo condition = null;
	private Tuple leftTuplePointer = null;
	private Tuple rightTuplePointer = null;
	private Tuple leftTuple = null;
	private Tuple rightTuple = null;
	private ArrayList<ColumnDetails> schema = null;
	private ArrayList<ColumnDetails> leftSchema = null;
	private ArrayList<ColumnDetails> rightSchema = null;
	private Map<String,Integer> leftSchemaIndex = null;
	private Map<String,Integer> rightSchemaIndex = null;
	private Column leftColumn = null;
	private Column rightColumn = null;
	private int leftIndex = -1;
	private int rightIndex = -1;
	private boolean isDone = false;
	private boolean init = false;
	private ArrayList<OrderByElement> leftOrderList = new ArrayList<OrderByElement>();
	private ArrayList<OrderByElement> rightOrderList = new ArrayList<OrderByElement>();
	private ArrayList<Tuple> leftTupleList = new ArrayList<Tuple>();
	private ArrayList<Tuple> rightTupleList = new ArrayList<Tuple>();
	private Iterator<Tuple> leftIterator = null;
	private Iterator<Tuple> rightIterator = null;
	
	public MergeJoin(Operator left, Operator right,EqualsTo exp) {
		super(left, right);
		this.condition  = exp;
		// TODO Auto-generated constructor stub
	}
	@Override
	public Tuple readTuple() {
		// TODO Auto-generated method stub
		if(!init){
			left = new ExternalSortOperator(leftOrderList, left);
			left.reset();
			right = new ExternalSortOperator(rightOrderList, right);
			right.reset();
			init = true;
			leftTuplePointer = left.readTuple();
			rightTuplePointer = right.readTuple();
			if(!loadTuples()){
				isDone = true;
				return null;
			}
			leftTuple = leftIterator.next();
		}
		
		if(rightIterator.hasNext()){
			rightTuple = rightIterator.next();
		}else if(leftIterator.hasNext()){
			leftTuple = leftIterator.next();
			rightIterator = rightTupleList.iterator();
			rightTuple = rightIterator.next();
		}else{
			if(!loadTuples()){
				isDone = true;
				return null;
			}else{
				leftTuple = leftIterator.next();
				rightTuple = rightIterator.next();
			}
		}
		
		Tuple tuple = new Tuple();
		tuple.append(leftTuple);
		tuple.append(rightTuple);
		
		return tuple;
	}
	
	private boolean loadTuples(){

		if(leftTuplePointer != null && rightTuplePointer != null){
			Datum leftDatum = leftTuplePointer.getValue(leftIndex);
			Datum rightDatum = rightTuplePointer.getValue(rightIndex);
			int compareValue = leftDatum.compareTo(rightDatum);
	
			do {
				if (compareValue == -1) {
					do {
						leftTuplePointer = left.readTuple();
						if(leftTuplePointer == null){
							return false;
						}
						leftDatum = leftTuplePointer.getValue(leftIndex);
						compareValue = leftDatum.compareTo(rightDatum);
					} while (compareValue == -1);
				}

				if (compareValue == 1) {

					do {
						rightTuplePointer = right.readTuple();
						if(rightTuplePointer == null){
							return false;
						}
						rightDatum = rightTuplePointer.getValue(rightIndex);
						compareValue = leftDatum.compareTo(rightDatum);
					} while (compareValue == 1);
				}
			} while (compareValue != 0);
			
			if(compareValue == 0){
				Datum simDatum = leftDatum;
				leftTupleList.clear();
				do{
					leftTupleList.add(leftTuplePointer);
					leftTuplePointer = left.readTuple();
					if(leftTuplePointer==null){
						break;
					}
					leftDatum = leftTuplePointer.getValue(leftIndex);
				}while(leftDatum.isEqualTo(simDatum));
				
				rightTupleList.clear();
				do{
					rightTupleList.add(rightTuplePointer);
					rightTuplePointer = right.readTuple();
					if(rightTuplePointer==null){
						break;
					}
					rightDatum = rightTuplePointer.getValue(rightIndex);
				}while(rightDatum.isEqualTo(simDatum));
				
				leftIterator = (Iterator<Tuple>)leftTupleList.iterator();
				rightIterator = (Iterator<Tuple>)rightTupleList.iterator();
			}
		}else{
			return false;
		}
		return true;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(left.toString());
		sb.append(" sort merge ");
		sb.append(right.toString());
		sb.append("]");
		return sb.toString();
	}

	public void loadBuffer(boolean isLeft){
	
	}
	@Override
	public void reset() {

		left.reset();
		right.reset();
		
		leftSchema = left.getSchema();
		rightSchema = right.getSchema();
		
		ColumnIndexParser sip = new ColumnIndexParser(leftSchema);
		// System.out.println("SelectList::"+selectList);
		for (ColumnDetails colDetails : leftSchema) {
			Column col = colDetails.getColumn();
			col.accept(sip);
		}

		leftSchemaIndex = sip.getResult();

		sip = new ColumnIndexParser(rightSchema);
		// System.out.println("SelectList::"+selectList);
		for (ColumnDetails colDetails : rightSchema) {
			Column col = colDetails.getColumn();
			col.accept(sip);
		}

		rightSchemaIndex = sip.getResult();

		Column leftCol = (Column) condition.getLeftExpression();
		Column rightCol = (Column) condition.getRightExpression();
				
		if (rightSchemaIndex.containsKey(leftCol.toString())) {
			leftColumn = rightCol;
			rightColumn = leftCol;
		} else {
			leftColumn = leftCol;
			rightColumn = rightCol;
		}

		OrderByElement le = new OrderByElement();
		le.setAsc(true);
		le.setExpression(leftColumn);
		
		OrderByElement re = new OrderByElement();
		re.setAsc(true);
		re.setExpression(rightColumn);
		leftOrderList.add(le);
		rightOrderList.add(re);

		
		leftIndex = leftSchemaIndex.get(leftColumn.getWholeColumnName());
		rightIndex = rightSchemaIndex.get(rightColumn.getWholeColumnName());
		
		schema = new ArrayList<ColumnDetails>();
		schema.addAll(left.getSchema());
		schema.addAll(right.getSchema());
		// Check left Operators table name and right operators table name 
	}

	@Override
	public boolean isDone() {
		return isDone;
		
	}
	
	public void clear(){
		
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

	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
	
/*	public static void main(String args[]){
		DataConfiguration.getInstance().setDataPath("E:\\Masters\\Spring 2014\\DB\\data\\one_gb");
		DataConfiguration.getInstance().setSwapPath("E:\\Masters\\Spring 2014\\DB\\data\\swap");
		ArrayList<String> fileList = new ArrayList<String>();
		fileList.add("E:\\Masters\\Spring 2014\\DB\\tpch_queries\\tpch_schemas.sql");
		QueryFileParser.parseFileList(fileList);
		System.out.println(DataConfiguration.getInstance().isSwap());
		Operator operator = new FromOperator(new Table(null, "nation"));
		operator = new RenameOperator(operator,"n1");
		Operator op = new FromOperator(new Table(null,"region"));
		op = new RenameOperator(op, "n2");
		EqualsTo eq = new EqualsTo();
		eq.setLeftExpression(new Column(new Table(null,"n1"),"regionkey"));
		eq.setRightExpression(new Column(new Table(null,"n2"),"regionkey"));
		operator = new MergeJoin(operator, op, eq);
		try {
			QueryExecutor.executeQuery(operator);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} */
}
