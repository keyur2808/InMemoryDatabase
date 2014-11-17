package edu.buffalo.cse562;

import java.util.ArrayList;

import edu.buffalo.cse562.configuration.DataConfiguration;
import edu.buffalo.cse562.parser.QueryFileParser;

public class Main{
	public static void main(String args[]){
		
		if(args.length < 3){
			printHelp();
			System.exit(1);
		}
		String dataPath = null;
		String swapPath = null;
		String indexPath= null;
		ArrayList<String> queryFile = new ArrayList<String>();
		
		if(args[0].equalsIgnoreCase("--data")){
			dataPath = args[1];
		}
		
		int i = 6;
		if(args[2].equalsIgnoreCase("--swap")){
			swapPath = args[3];
			if (args[4].equalsIgnoreCase("--index")){
				indexPath = args[5];
			}else{
				i=4;
			}
		}
		else{
			if (args[2].equalsIgnoreCase("--index")){
				indexPath = args[3];
				i=4;
			}else{
				i=2;
			}		
		}
		
		
		
		for(;i<args.length;i++){
				queryFile.add(args[i]);
		}
		
		if(queryFile.isEmpty() || dataPath == null){
			printHelp();
			System.exit(1);
		}
		
		DataConfiguration config = DataConfiguration.getInstance();
		config.setDataPath(dataPath);
		if(swapPath!=null)
			config.setSwapPath(swapPath);
		if (indexPath!=null)
			config.setIndexPath(indexPath);
		QueryFileParser.parseFileList(queryFile);
		// Call queryFileParser
	}
	
	public static void printHelp(){
		System.out.println("The usage is edu.buffalo.cse562.Main --data <data_directory> <sql_file_list>");
		System.out.println("data_directory : The Directory in which the data is stored");
		System.out.println("sql_file : The directory in which the sql queries are placed");
	}
}