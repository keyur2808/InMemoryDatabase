package edu.buffalo.cse562.Executor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

import edu.buffalo.cse562.operator.Operator;
import edu.buffalo.cse562.data.*;

public class QueryExecutor {

	public static void executeQuery(Operator operator) throws IOException{
	//	System.out.println(operator.toString());
		BufferedWriter bw = null;
		try{
		long start = System.currentTimeMillis();
		operator.reset();
		Tuple tuple = null;
		int i = 0;
		 bw = new BufferedWriter(new PrintWriter(System.out));
		do{
			tuple = operator.readTuple();
			if(tuple != null){
				i++;
					bw.write(tuple.toString());
			//		bw.write("\n");
			//	System.out.println(tuple.toString());
			}
			if(i %1000 == 0){
				bw.flush();
			}
		}while(tuple != null);
		bw.flush();
		long end = System.currentTimeMillis();
	//	System.out.println("Completed in "+(end-start)+"millis");
	//	System.out.println(i);
		}/*catch(IOException e){
			e.printStackTrace();
		}*/finally{
			try{
			//	bw.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}