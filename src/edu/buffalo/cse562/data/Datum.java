package edu.buffalo.cse562.data;

import java.io.Serializable;

import net.sf.jsqlparser.expression.Expression;

public interface Datum extends Serializable,Expression{

	public Object getVal();
	public Datum getDatum(String s);
	public boolean isEqualTo(Datum d);
	public boolean greaterThan(Datum d);
	public boolean lesserThan(Datum d);	
	public Datum add(Datum d);
	public Datum mul(Datum d);
	public Datum sub(Datum d);	
	public Datum div(Datum d);
	public int compareTo(Datum d);
	public int hashCode();
	@Override
	public boolean equals(Object d);
	public void accept(DatumExpressionVisitor dev);
}
