package edu.buffalo.cse562.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TupleComparator implements Comparator<Object>{
	private int onColumn;
	private ArrayList<Integer> onColumns;
	private ArrayList<Boolean> colOrder;

	public int getOnColumn() {
		return onColumn;
	}

	public void setOnColumn(int onColumn) {
		this.onColumn = onColumn;
	}
	
	public void setColumnList(List<Integer> onColumns){
		this.onColumns = (ArrayList<Integer>) onColumns;
	}

	@Override
	public int compare(Object o1, Object o2) {
		Tuple t1 = (Tuple) o1;
		Tuple t2 = (Tuple) o2;
		if(t1!=null && t2!=null){
			int val = 0;
			int i = 0;
			do{
				onColumn = onColumns.get(i);
				if(colOrder.get(i)){
					val = t1.getValue(onColumn).compareTo(t2.getValue(onColumn));
				}else{
					val = t2.getValue(onColumn).compareTo(t1.getValue(onColumn));
				}
				i++;
			}while(val==0 && i < onColumns.size());
			return val;
		}
		else 
			return 0;
	}

	public void setOrder(List<Boolean> colOrder) {
		// TODO Auto-generated method stub
		this.colOrder = (ArrayList<Boolean>) colOrder;		
	}

}
