package edu.buffalo.cse562.operator;

import net.sf.jsqlparser.schema.Column;
import edu.buffalo.cse562.data.Tuple;

public class PartitionUtil {

	public static int MAX_COUNT = 15;

	public static int getPartition(Tuple tuple, Column keyColumn, Integer columnIndex) {
		// TODO Auto-generated method stub
		int hashCode = tuple.getValue(columnIndex).hashCode();
		return Math.abs(hashCode) % (MAX_COUNT + 1);
	}
}
