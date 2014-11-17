package edu.buffalo.cse562.data;

import net.sf.jsqlparser.expression.ExpressionVisitor;


public class ForString implements Datum{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9190506186526773556L;
	private String val;
	
	public ForString(){}
	
	public ForString(String s){
		val = s;
	}

	@Override
	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}
	
	@Override
	public ForString getDatum(String value){
		setVal(value);
		return this;
	}

	@Override
	public boolean isEqualTo(Datum d) {
		String a=val;
		String b=(String)d.getVal();
		return a.equals(b);
	}

	@Override
	public boolean greaterThan(Datum d) {
		int diff = val.compareTo((String)d.getVal());
		if (diff>0)
			return true;
		else
			return false;
	}

	@Override
	public boolean lesserThan(Datum d) {
		int diff = val.compareTo((String)d.getVal());
		if (diff<0)
			return true;
		else
			return false;
	}

	@Override
	public Datum add(Datum d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datum mul(Datum d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datum sub(Datum d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datum div(Datum d) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Datum concat(Datum d){
		String a = getVal();
		String b = d.getVal().toString();
		return (new ForString(a.concat(b)));
	}
	
	@Override
	public int compareTo(Datum d) {
		return val.compareTo((String)d.getVal());
	}
	
	public String toString(){
		return this.val.toString();
	}
	
	@Override
	public int hashCode(){
		return this.val.hashCode();
	}
	
	@Override
	public boolean equals(Object d){
		return this.isEqualTo((Datum)d);
	}
	
	public void accept(DatumExpressionVisitor dev){
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
