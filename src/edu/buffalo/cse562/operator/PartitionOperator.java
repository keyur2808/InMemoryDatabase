package edu.buffalo.cse562.operator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.data.Tuple;
import edu.buffalo.cse562.queryoptimizer.OperatorVisitor;

public class PartitionOperator implements Operator {

	private Operator input = null;
	private Column keyColumn = null;
	private ArrayList<ColumnDetails> schema;
	private Integer columnIndex;
	private ArrayList<Partition> partitionList = new ArrayList<Partition>();
	private Partition currPartition = null;
	private int currIndex= 0;
	private boolean init = false;
	private boolean isDone = false;
	private HashSet<Integer> partitionIdList = null;
	private boolean rightPartition = false;

	public PartitionOperator(Operator input,Column keyColumn, Integer columnIndex){
		this.input = input;
		this.keyColumn = keyColumn;
		this.columnIndex = columnIndex;
	}
	@Override
	public Tuple readTuple() {
		// TODO Auto-generated method stub
		if(!init){
			initialize();
			init = true;
			currPartition = partitionList.get(currIndex++);
		}
		
		Tuple tuple = null;
		if(!currPartition.isDone()){
			tuple = currPartition.readNext();
			if(tuple == null){
				this.isDone = true;
			}
			return tuple;
		}
		/*	if(tuple == null){
				if(partitionList.size() == currIndex){
					return null;
				}else{
					do{
						currPartition = partitionList.get(currIndex++);
						tuple = currPartition.readNext();
					}while(tuple!=null || currIndex == partitionList.size());
				}
			}
			return tuple;
		}*/
		
		return null;
	}
	
	public boolean loadNextPartition(){
		currPartition.closeReader();
		if(currIndex < partitionList.size()){
			currPartition = partitionList.get(currIndex++);
			this.isDone = false; 
			return true;
		}
		return false;
	}

	public HashSet<Integer> getPartitionIdList(){
		HashSet<Integer> partInt = new HashSet<Integer>();
		for(Partition part : partitionList){
			partInt.add(part.getIdentifier());
		}
		return partInt;
	}
	
	private void initialize() {
		// TODO Auto-generated method stub
		Tuple tuple = null;
		do{
			tuple = input.readTuple();
			if(tuple!=null){
				int partitionid = PartitionUtil.getPartition(tuple,keyColumn,columnIndex);
				Partition partition = partitionList.get(partitionid);
				if(!rightPartition ){
					partition.write(tuple);
				}else if(partitionIdList!=null && partitionIdList.contains(partitionid)){
					partition.write(tuple);
				}
			}
		}while(tuple!=null);
		for(Partition partition : partitionList){
			partition.closeWriter();
		}

	}
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		schema = input.getSchema();
		for(int i=0;i<=PartitionUtil.MAX_COUNT;i++){
			partitionList.add(new Partition(i,keyColumn.getWholeColumnName(),schema));
		}

	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return isDone;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<ColumnDetails> getSchema() {
		// TODO Auto-generated method stub
		return schema;
	}

	@Override
	public Map<String, Integer> getSchemaIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OperatorVisitor ov) {
		// TODO Auto-generated method stub

	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(input.toString());
		return sb.toString();
	}
	
	public int getDataCount(){
		return currPartition.getDataCount();
	}
	public void setPartitionIdList(HashSet<Integer> partitionIdList) {
		// TODO Auto-generated method stub
		this.partitionIdList  = partitionIdList;
		this.rightPartition = true;
	}

}
