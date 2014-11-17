package edu.buffalo.cse562.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import edu.buffalo.cse562.Executor.QueryExecutor;
import edu.buffalo.cse562.operator.Operator;
import edu.buffalo.cse562.queryoptimizer.EquivalenceGenerator;
import edu.buffalo.cse562.visitor.StatementParser;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;

public class QueryFileParser {

	public static void parseFileList(ArrayList<String> fileList) {
		StatementParser sp = null; 
		for (String filePath : fileList) {
			File f = new File(filePath);
			try {
				FileReader stream = new FileReader(f);
				CCJSqlParser parser = new CCJSqlParser(stream);
				Statement stmt;
				while ((stmt = parser.Statement()) != null) {
					sp = new StatementParser();
					stmt.accept(sp);	
					Operator op = sp.getOperator();
					if(op!=null){
				//		System.out.println(op.toString());
						EquivalenceGenerator eqGen = new EquivalenceGenerator();
						op.accept(eqGen);
						Operator newOp =  eqGen.getOperator();
				//		System.out.println(newOp.toString());
						//Operator reformation
						QueryExecutor.executeQuery(newOp);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to parse the file " + f);
			} catch (net.sf.jsqlparser.parser.ParseException e) {
				e.printStackTrace();
				System.out.println("Unable to parse the query in file " + f);
			}
		}
	}
}