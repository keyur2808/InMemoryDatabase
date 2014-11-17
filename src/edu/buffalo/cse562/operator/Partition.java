package edu.buffalo.cse562.operator;

import java.io.File;
import java.util.ArrayList;

import edu.buffalo.cse562.configuration.ColumnDetails;
import edu.buffalo.cse562.configuration.DataConfiguration;
import edu.buffalo.cse562.data.Tuple;

public class Partition {

	private String fileName = null;
	private PartitionWriter pw = null;
	private PartitionReader pr = null;
	private boolean isDone = false;
	private int dataCount = 0;
	private ArrayList<ColumnDetails> schema = null;
	private Integer identifier = 0;
	
	public Partition(int i, String columnName, ArrayList<ColumnDetails> schema){
		this.identifier = i;
		this.fileName = DataConfiguration.getInstance().getSwapPath() + File.separator + columnName + i +".txt";
		this.schema = schema;
	}
	
	public void write(Tuple tuple){
		dataCount++;
		if(pw == null){
			pw = new PartitionWriter(fileName);
		}
		pw.write(tuple);
		if(dataCount%10000 == 0){
			pw.flush();
		}
	}

	public boolean isDone() {
		// TODO Auto-generated method stub
		return isDone;
	}

	public Tuple readNext() {
		// TODO Auto-generated method stub
		if(dataCount == 0){
			return null;
		}
		if(pr == null){
			pr = new PartitionReader(fileName,schema);
		}
		Tuple tuple = pr.readNext();
		if(tuple == null){
			this.isDone = true;
			this.closeReader();
			this.deletFile();
		}
		return tuple;
	}

	private void deletFile() {
		// TODO Auto-generated method stub
		File f = new File(fileName);
		f.delete();
	}

	public void closeWriter() {
		// TODO Auto-generated method stub
		if(pw != null){
			pw.flush();
			pw.close();
		}
	}

	public void closeReader() {
		// TODO Auto-generated method stub
		if(pr!= null){
			pr.close();
		}
	}

	public Integer getIdentifier() {
		// TODO Auto-generated method stub
		return this.identifier;
	}

	public int getDataCount() {
		// TODO Auto-generated method stub
		return dataCount;
	}
}
