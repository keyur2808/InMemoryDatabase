package edu.buffalo.cse562.operator;

import java.util.List;

import edu.buffalo.cse562.data.Tuple;

public class OperatorUtil {
	public static void loadBuffer(Operator input,List<Tuple> buffer){
		int count = 0;
		Tuple tuple = null;
	//	System.out.println(input);
		do{
			tuple = input.readTuple();
			if(tuple!=null){
			buffer.add(tuple);
			}
			count++;
		}while(tuple != null && count <1000);
	}
}
