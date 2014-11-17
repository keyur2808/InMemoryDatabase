package edu.buffalo.cse562.data;

import net.sf.jsqlparser.expression.ExpressionVisitor;


public class ForLong implements Datum{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8813299158584573595L;
	private Long val;

	public ForLong(){
		
	}
	
	public ForLong(Long v){
		val=v;
	}
	@Override
	public Long getVal() {
		return val;
	}

	public void setVal(long val) {
		this.val = new Long(val);
	}
		
	@Override
	public ForLong getDatum(String value){
		setVal(Long.parseLong(value));
		return this;
	}

	@Override
	public boolean isEqualTo(Datum d) {
		long a = getVal();
		long b = (Long) d.getVal();
		return a==b;
	}

	@Override
	public boolean greaterThan(Datum d) {
		long a = getVal();
		long b = (Long) d.getVal();
		return a>b;
	}

	@Override
	public boolean lesserThan(Datum d) {
		long a = getVal();
		long b = (Long) d.getVal();
		return a<b;
	}

	@Override
	public Datum add(Datum d) {
		return new ForLong(val+((Long)d.getVal()));
	}

	@Override
	public Datum mul(Datum d) {
		return new ForLong(val*((Long)d.getVal()));
	}

	@Override
	public Datum sub(Datum d) {
		return new ForLong(val-((Long)d.getVal()));
	}

	@Override
	public Datum div(Datum d) {
		return new ForLong(val/((Long)d.getVal()));
	}
	
	public Datum bitAnd(Datum d){
		long a = getVal();
		long b = (Long) d.getVal();
		return (new ForLong(a & b));
	}
	
	public Datum bitOr(Datum d){
		long a = getVal();
		long b = (Long) d.getVal();
		return (new ForLong(a | b));
	}
	
	public Datum bitXor(Datum d){
		long a = getVal();
		long b = (Long) d.getVal();
		return (new ForLong(a ^ b));
	}

	@Override
	public int compareTo(Datum d) {
		long a = getVal();
		long b = (Long) d.getVal();
		if(a < b){
			return -1;
		}
		else if(a == b){
			return 0;
		}
		else{
			return 1;
		}
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
