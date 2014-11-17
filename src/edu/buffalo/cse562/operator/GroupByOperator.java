package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectItem;
import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;
import edu.buffalo.cse562.visitor.AggregateParser;
import edu.buffalo.cse562.visitor.ColumnIndexParser;

public class GroupByOperator implements Operator{

	private Operator input = null;
	private List<SelectItem> selectList = null;
	private List<SelectItem> colList = null;
	private List<Column> groupByCols = null;
	private ArrayList<ColumnDetails> schema = null;
	private Map<String,Integer> schemaIndex = null;
	private boolean isInit = false;
	private boolean isDone = false;
	private Map<String,Tuple> buffer = null;
	private Iterator<String> it = null;
	private List<Integer> indexList = new ArrayList<Integer>();
	private AggregateParser aggParser = null;
	
	public GroupByOperator(Operator input,List<SelectItem> selectList,List<Column> groupByCols){
		this.input = input;
		this.selectList = selectList;
		this.groupByCols = groupByCols;
		this.buffer = new HashMap<String,Tuple>();
		
	}
	
	public Operator getOperator(){
		return input;
	}
	@Override
	public Tuple readTuple() {
		if(!isInit){
		//	System.out.println("GroupBy"+ groupByCols);
			initialize();
		}
		if(buffer == null || buffer.isEmpty()){
		//	System.out.println("End of GroupBy"+ groupByCols);
			this.isDone = true;
			return null;
		}
		if(it.hasNext()){
			String key =  it.next();
			return buffer.get(key);
		}
		this.isDone = true;
	//	System.out.println("End of GroupBy"+ groupByCols);
		return null;
	}
	
	private void initialize(){
		Tuple tuple = null;
//		System.out.println("Initialize Start");
		do{
			tuple = input.readTuple();
			if(tuple != null){
				groupBy(tuple);
			}
		}while(tuple != null);
//		System.out.println("initialize over");
	//	System.out.println(buffer);
		it = buffer.keySet().iterator();
		isInit = true;
	//	System.out.println(buffer.get("Willie||"));
	}
	
	private void groupBy(Tuple tuple){
		StringBuilder sb = new StringBuilder();
		//System.out.println(tuple);
	//	System.out.println(schemaIndex);
	//	System.out.println(schema);
		//System.out.println(tuple);
		if(groupByCols != null){
			for(Integer index : indexList){
				sb.append(tuple.getValue(index).toString());
				sb.append("||");
			}
		}
		
		String hashString = sb.toString();
	//	System.out.println(hashString);
		Tuple outputTuple = null;
		if(buffer.containsKey(hashString)){
			outputTuple = buffer.get(hashString);
		}
		if(outputTuple == null ){
			outputTuple = new Tuple();
			outputTuple.append(tuple);
			int extendSize = schema.size() - outputTuple.getValues().size();
			for(int i=0;i<extendSize;i++){
				outputTuple.addValue(null);
			}
		}
	//	System.out.println(tuple);
		//System.out.println(tuple);
		aggParser.setTuples(tuple, outputTuple);
		aggParser.setKey(hashString);
		for(SelectItem selectExpItem : colList){
			//System.out.println(colList);
			selectExpItem.accept(aggParser);
		}
		
		outputTuple = aggParser.getAggregatedTuple();
		//System.out.println(outputTuple);
		buffer.put(hashString, outputTuple);
		// run aggregate functions over the selectItems!!!
		// Add the values to tuple
		// Add the tuple to the map 
	}
	

	@Override
	public void reset() {
		input.reset();
		loadSchema();
		checkSchema();
		//System.out.println("Groupby::"+schema);
		getGroupByIndex();
		loadAggregateParser();
	}
	
	private void loadAggregateParser() {
		this.aggParser = new AggregateParser(schemaIndex);
	}
	public void getGroupByIndex(){
		if(groupByCols != null){
			for(Column col : groupByCols){
				Integer index = schemaIndex.get(col.getWholeColumnName());
				indexList.add(index); 
			}
		}
	}
	
	private void loadSchema(){
		schema = input.getSchema();
	}
	
	public void checkSchema(){
		ColumnIndexParser sip = new ColumnIndexParser(schema);
		//System.out.println("SelectList::"+selectList);
		for(SelectItem selectExpItem : selectList){
			selectExpItem.accept(sip);
		}
		List<SelectItem> newCols = sip.getNewList();
		//System.out.println("NewCols::"+newCols);
		colList = new ArrayList<SelectItem>();
		colList.addAll(newCols);
		colList.addAll(selectList);
	/*	for(Column col : groupByCols){
			col.accept(sip);
		} */
		schemaIndex = sip.getResult();
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
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(input);
		sb.append(" Group by");
		sb.append(groupByCols);
		sb.append("]");
		return sb.toString();
	}
	@Override
	public Map<String, Integer> getSchemaIndex() {
		return schemaIndex;
	}
	@Override
	public void accept(OperatorVisitor ov) {
		ov.visit(this);
	}
	
	public List<SelectItem> getSelectList(){
		return selectList;
	}
	
	public List<Column> getGroupByColumns(){
		return groupByCols;
	}
}
