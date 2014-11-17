package edu.buffalo.cse562.data;

import java.sql.Date;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;


public class ForDate implements Datum,DatumExpression{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5064298100608061246L;
	private Date date;

	@Override
	public Date getVal() {
		return date;
	}

	public void setVal(String value) {
		if(value.equals(""))
			date=null;
		else{
			date = Date.valueOf(value);
		}
	}
	
	@Override
	public ForDate getDatum(String value){
		setVal(value);
		return this;
	}

	@Override
	public boolean isEqualTo(Datum d) {
		return (date.compareTo(((ForDate)d).getVal()) == 0);		
	}

	@Override
	public boolean greaterThan(Datum d) {	
		return (date.compareTo(((ForDate)d).getVal()) > 0);
	}

	@Override
	public boolean lesserThan(Datum d) {
		return (date.compareTo(((ForDate)d).getVal()) < 0);
	}

	@Override
	public Datum add(Datum d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datum mul(Datum d) {
		return null;
	}

	@Override
	public Datum sub(Datum d) {
		return null;
	}

	@Override
	public Datum div(Datum d) {
		return null;
	}
	
	@Override
	public int hashCode(){
		return this.date.hashCode();
	}
	
	@Override
	public int compareTo(Datum d) {
		return date.compareTo(((ForDate)d).getVal());		
	}
	
	public String toString(){
		return date.toString();
	}

	
	@Override
	public boolean equals(Object o){		
		return isEqualTo((Datum) o);
	}

	@Override
	public void accept(DatumExpressionVisitor dev) {
		// TODO Auto-generated method stub
		dev.visit(this);
	}

	@Override
	public void accept(ExpressionVisitor arg0) {
		// TODO Auto-generated method stub
		if(arg0 instanceof DatumExpressionVisitor){
			((DatumExpressionVisitor)arg0).visit(this);
		}
	}
	
}
