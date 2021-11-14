package com.jimmiker;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Decimal 
{
	BigDecimal data;
	//byte[] data;
	public Decimal()
	{
		data = BigDecimal.ZERO;
	}
	
	public Decimal(byte[] getdata)
	{
		int sign = getdata[0] == 0x00 ? 1 : -1;
		int exponent = getdata[1];
		byte[] num = new byte[14];
		System.arraycopy(getdata, 2, num, 0, 14);
		BigInteger integer = new BigInteger(num);
		integer = integer.multiply(BigInteger.valueOf(sign));
		data = new BigDecimal(integer, exponent);
	}
	
	public Decimal(BigDecimal getdata)
	{
		data = getdata;
		data = new Decimal(toByteArray()).data;
	}
	
	public Decimal(String getdata)
	{
		data = new BigDecimal(getdata);
		data = new Decimal(toByteArray()).data;
	}
	
	public byte[] toByteArray()
	{
		BigInteger integer = data.unscaledValue();
		int exponent = data.scale();
		int sign = data.signum();
		byte[] num = integer.toByteArray();
		byte[] ans = new byte[16];
		ans[0] = sign == -1 ? (byte)0x80 : 0x00;
		ans[1] = (byte)exponent;
		System.arraycopy(num, 0, ans, num.length <= 14 ? ans.length - num.length : 2, num.length <= 14 ? num.length : 14);
		
		return ans;
	}
	
	public Decimal add(Decimal val) 
	{
		return new Decimal(data.add(val.data));
	}
	
	public Decimal subtract(Decimal val) {
		return new Decimal(data.subtract(val.data));
	}
	
	public Decimal multiply(Decimal val) {
		return new Decimal(data.multiply(val.data));
	}

	public Decimal divide(Decimal val) {
		return new Decimal(data.divide(val.data));
	}

	public Decimal abs() {
		return new Decimal(data.abs());
	}

	public Decimal negate() {
		return new Decimal(data.negate());
	}

	public Decimal pow(int n) {
		return new Decimal(data.pow(n));
	}

	public Decimal max(Decimal val) {
		return new Decimal(data.max(val.data));
	}
	
	public Decimal min(Decimal val) {
		return new Decimal(data.min(val.data));
	}
	
	public BigDecimal toBigDecimal() 
	{
		return new Decimal(data).data;
	}

	@Override
	public String toString() 
	{
		return data.toPlainString();
	}
}
