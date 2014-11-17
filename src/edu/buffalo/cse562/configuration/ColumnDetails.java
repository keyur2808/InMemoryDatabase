package edu.buffalo.cse562.configuration;

import java.lang.Cloneable;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

public class ColumnDetails implements Cloneable {

	private Column column;
	private ColumnDefinition columnDefinition;
	@SuppressWarnings("unused")
	private String expression;

	public ColumnDetails(Column column, ColumnDefinition columnDefinition) {
		this.column = column;
		this.columnDefinition = columnDefinition;
	}

	public ColumnDetails() {
		// TODO Auto-generated constructor stub
	}

	public void setColumn(Column column) {
		this.column = column;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}

	public void setColumnDefinition(ColumnDefinition columnDefinition) {
		this.columnDefinition = columnDefinition;
	}

	public String toString() {
		if(this.column.getTable()!=null){
			return this.column.getWholeColumnName();
		}else{
			return this.column.getColumnName();
		}
	}

	public Column getColumn() {
		return this.column;
	}

	public ColumnDefinition getColumnDefinition() {
		return this.columnDefinition;
	}

	public boolean equals(ColumnDetails columnDetails) {
		Column col = columnDetails.getColumn();
		ColumnDefinition colDef = columnDetails.getColumnDefinition();
		if (column.getWholeColumnName().equalsIgnoreCase(
				col.getWholeColumnName())) {
			return true;
		} else if (column.getColumnName().equalsIgnoreCase(col.getColumnName())) {
			if (this.columnDefinition.getColDataType().equals(
					colDef.getColDataType())) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	public Object clone() throws CloneNotSupportedException {
		ColumnDefinition colDef = null;
		if (columnDefinition != null) {
			colDef = new ColumnDefinition();
			colDef.setColDataType(columnDefinition.getColDataType());
			colDef.setColumnName(columnDefinition.getColumnName());
			colDef.setColumnSpecStrings(columnDefinition.getColumnSpecStrings());
		}
		Column col = null;
		if (column != null) {
			col = new Column();
			col.setColumnName(column.getColumnName());
			col.setTable(column.getTable());
		}
		return new ColumnDetails(col, colDef);
	}
}