package edu.buffalo.cse562.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tuple implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -48577900610611450L;
	private List<Datum> cols;
	
	public Tuple(){
		cols = new ArrayList<Datum>();
	}
	
	public void addValue(Datum value){
		cols.add(value);
	}
	
	public void setValue(int index,Datum value){
		if(cols.size()<=index){
			cols.add(value);
		}else{
			cols.set(index,value);
		}
	}
	
	public Datum getValue(int i){
		return cols.get(i);
	}
	
	public List<Datum> getValues(){
		return cols;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (Datum obj : cols){
			if(obj!=null){
				sb.append(obj.toString());
			}
			sb.append("|");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("\n");
		return sb.toString();
	}
	
	public void append(Tuple tuple)
	{
		cols.addAll(tuple.getValues());
	}
}
