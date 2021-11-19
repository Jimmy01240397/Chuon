package com.jimmiker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChuonBinary {
	
    byte[] data;
    public ChuonBinary(Object thing) throws Exception
    {
    	try(ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            data = Typing(stream, thing);
            stream.close();
        }
    }

    public ChuonBinary(byte[] thing) throws Exception
    {
        data = Arrays.copyOf(thing, thing.length);
        toObject();
    }

    public Object toObject() throws Exception
    {
    	Object output = null;
        if (data != null)
        {
        	try(ByteArrayInputStream stream = new ByteArrayInputStream(data))
        	{
	        	try(DataInputStream reader = new DataInputStream(stream))
	        	{
	                output = GetTyp(reader);
	                reader.close();
	                stream.close();
	        	}
        	}
        }
        return output;
    }

    public byte[] toArray()
    {
        if (data != null)
        {
            return Arrays.copyOf(data, data.length);
        }
        return null;
    }

    public ChuonString toChuonString() throws Exception
    {
        Object datas = toObject();
        return new ChuonString(datas);
    }
	
    public static byte[] GetBytesLength(int cont) throws IOException
    {
    	ByteArrayOutputStream data = new ByteArrayOutputStream();
    	DataOutputStream vs = new DataOutputStream(data);
        for (int i = cont / 128; i != 0; i = cont / 128)
        {
            vs.writeByte((byte)(cont % 128 + 128));
            cont = i;
        }
        vs.writeByte((byte)(cont % 128));
        vs.close();
        data.close();
        return data.toByteArray();
    }
    
    public static int GetIntLength(DataInputStream reader) throws IOException
    {
    	ArrayList<Integer> vs = new ArrayList<Integer>();
    	int a;
        do
        {
        	a = reader.readByte() & 0x0FF;
            vs.add(a % 128);
        } while (a >= 128);
        int x = 0;
        for (int i = 0; i < vs.size(); i++)
        {
            x += (int)(vs.get(i) * Math.pow(128, i));
        }
        return x;
    }
    
    static byte[] byteSerialize(byte[] ans) throws IOException
    {
   	    byte[] len = GetBytesLength(ans.length);
   	    byte[] output = new byte[ans.length + len.length];
   	    System.arraycopy(len, 0, output, 0, len.length);
   	    System.arraycopy(ans, 0, output, len.length, ans.length);
   	    return output;
    }
    
    static void TypingArray(ByteArrayOutputStream stream, Object thing) throws Exception
    {
    	if(!thing.getClass().isArray())
    	{
    		throw new IllegalArgumentException("Argument is not an array");
    	}
        //Array c = (Array)thing;
    	DataOutputStream writer = new DataOutputStream(stream);
        String typename = thing.getClass().getSimpleName();
        writer.writeByte((byte)(Arrays.asList(TypeFormat.type).indexOf(typename)));
        writer.write(GetBytesLength(Array.getLength(thing)));
        typename = StringTool.RemoveString(typename,"\\[", "\\]");
        thing = TypeFormat.PrimitiveAndClassArray(thing);
        if (typename.equals("Byte"))
        {
            byte[] c = (byte[])thing;
            writer.write(c);
        }
        else if(typename.equals("Character") || typename.equals("Boolean"))
        {
        	Class nowtype = Class.forName("java.lang." + typename);
        	Method bufferput = DataOutputStream.class.getMethod("write" + TypeFormat.type3[Arrays.asList(TypeFormat.type).indexOf(typename)], typename.equals("Character") ? int.class : boolean.class);
            for (int i = 0; i < Array.getLength(thing); i++)
            {
            	bufferput.invoke(writer, Array.get(thing, i));
            }
		}
        else if(typename.equals("Object") || typename.equals("Decimal") || typename.equals("String"))
        {
            for (int i = 0; i < Array.getLength(thing); i++)
            {
                if (typename.equals("Object"))
                {
                    writer.close();
                    Typing(stream, Array.get(thing, i));
                    writer = new DataOutputStream(stream);
                }
                else if (typename.equals("Decimal")) {
                	writer.write(TypeFormat.ArrayReverse(((Decimal)Array.get(thing, i)).toByteArray()));
				}
                else if (typename.equals("String"))
                {
                	byte[] nowdata = null;
                	switch (typename) 
                	{
    					case "String":
    					{
    	               	    nowdata = ((String)Array.get(thing, i)).getBytes(Charset.forName("UTF-8"));
    						break;
    					}
    				}
            		writer.write(byteSerialize(nowdata));
				}
            }
        }
        else
        {
        	Class nowtype = Class.forName("java.lang." + typename);
        	ByteBuffer buffer = ByteBuffer.allocate(nowtype.getField("BYTES").getInt(null));
        	Method bufferput = ByteBuffer.class.getMethod("put" + typename, TypeFormat.ChangeType.get(typename));
            for (int i = 0; i < Array.getLength(thing); i++)
            {
                writer.write(TypeFormat.ArrayReverse(((ByteBuffer)bufferput.invoke(buffer, Array.get(thing, i))).array()));
            }
		}
        writer.close();
	}
    
    static void TypingNotArray(ByteArrayOutputStream stream, Object thing) throws Exception
    {
        String typename = thing.getClass().getSimpleName();
    	DataOutputStream writer = new DataOutputStream(stream);
        writer.writeByte((byte)(Arrays.asList(TypeFormat.type).indexOf(typename)));

        if (typename.equals("Byte") || typename.equals("Character") || typename.equals("Boolean"))
        {
        	Method bufferput = DataOutputStream.class.getMethod("write" + TypeFormat.type3[Arrays.asList(TypeFormat.type).indexOf(typename)], typename.equals("Byte") || typename.equals("Character") ? int.class : boolean.class);
            bufferput.invoke(writer, thing);
        }
        else if(typename.equals("String"))
        {
        	byte[] nowdata = null;
        	switch (typename) 
        	{
				case "String":
				{
               	    nowdata = ((String)thing).getBytes(Charset.forName("UTF-8"));
					break;
				}
			}
    		writer.write(byteSerialize(nowdata));
        }
        else if (typename.equals("Decimal")) {
        	writer.write(TypeFormat.ArrayReverse(((Decimal)thing).toByteArray()));
		}
        else {
        	Class nowtype = Class.forName("java.lang." + typename);
        	ByteBuffer buffer = ByteBuffer.allocate(nowtype.getField("BYTES").getInt(null));
        	Method bufferput = ByteBuffer.class.getMethod("put" + typename, TypeFormat.ChangeType.get(typename));
        	
            writer.write(TypeFormat.ArrayReverse(((ByteBuffer)bufferput.invoke(buffer, thing)).array()));
		}
    }

    public static byte[] Typing(ByteArrayOutputStream stream, Object thing) throws Exception
    {
    	DataOutputStream writer = new DataOutputStream(stream);
        if (thing != null)
        {
        	thing = thing.getClass().getName().contains("[") && !thing.getClass().getName().contains("[]") ? TypeFormat.PrimitiveAndClassArray(thing) : thing;
        	String typename = Arrays.asList(TypeFormat.type).indexOf(thing.getClass().getSimpleName()) >= 0 ? thing.getClass().getSimpleName() : thing.getClass().getInterfaces()[0].getSimpleName();
            
        	if(typename.contains("[]"))
        	{
        		TypingArray(stream, thing);
        	}
        	else if (typename.equals("Map")) {
                Map c = (Map)thing;
                Class datatype = thing.getClass();
                Object[][] data = new Object[][] {c.keySet().toArray(), c.values().toArray()} ;
                Class[] Subdatatype = new Class[2];
                for(int i = 0; i < data.length; i++)
                {
                    for(int j = 0; j < data[i].length; j++)
                    {
                    	Class nowclass = Arrays.asList(TypeFormat.type).indexOf(data[i][j].getClass().getSimpleName()) >= 0 ? data[i][j].getClass() : data[i][j].getClass().getInterfaces()[0];
                    	if(Subdatatype[i] == null || Subdatatype[i] == nowclass)
                    	{
                    		Subdatatype[i] = nowclass;
                    	}
                    	else 
                    	{
							Subdatatype[i] = Object.class;
						}
                    }
                    if(Subdatatype[i] == null)
                    {
						Subdatatype[i] = Object.class;
                    }
                }
                writer.writeByte((byte)(Arrays.asList(TypeFormat.type).indexOf(typename)));
                writer.writeByte((byte)(Arrays.asList(TypeFormat.type).indexOf(Subdatatype[0].getSimpleName())));
                writer.writeByte((byte)(Arrays.asList(TypeFormat.type).indexOf(Subdatatype[1].getSimpleName())));
                writer.write(GetBytesLength(c.size()));

                for (int i = 0; i < c.size(); i++)
                {
                	writer.close();
                    Typing(stream, data[0][i]);
                    Typing(stream, data[1][i]);
                    writer = new DataOutputStream(stream);
                }
			}
        	else {
				TypingNotArray(stream, thing);
			}
        }
        else
        {
            writer.write((byte)(Arrays.asList(TypeFormat.type).indexOf("null")));
            writer.writeBoolean(false);
        }
        
        writer.close();
        return stream.toByteArray();
    }
    
    static Object GetTypArray(String typ, DataInputStream reader) throws Exception
    {
    	Object d = null;
        int count = GetIntLength(reader);
        typ = StringTool.RemoveString(typ, "\\[", "\\]");
        if (typ.equals("Byte"))
        {
            d = new byte[count];
            reader.read((byte[])d, 0, ((byte[])d).length);
            return d;
        }
        else if (typ.equals("Character") || typ.equals("Boolean"))
        {
        	Method bufferread = DataInputStream.class.getMethod("read" + TypeFormat.type3[Arrays.asList(TypeFormat.type).indexOf(typ)]);
            d = Array.newInstance(TypeFormat.TypeNameToType(typ), count);
            for (int i = 0; i < count; i++)
            {
            	Array.set(d, i, bufferread.invoke(reader));
            }
        }
        else if(typ.equals("Object") || typ.equals("Decimal") || typ.equals("String"))
        {
            d = Array.newInstance(TypeFormat.TypeNameToType(typ), count);
            for (int i = 0; i < count; i++)
            {
                if (typ.equals("Object"))
                {
	            	Array.set(d, i, GetTyp(reader));
                }
                else if (typ.equals("Decimal"))
                {
                	byte[] nowdata = new byte[16];
                	reader.read(nowdata, 0, nowdata.length);
                	Array.set(d, i, new Decimal(TypeFormat.ArrayReverse(nowdata)));
                }
                else if (typ.equals("String"))
                {
                	int size = GetIntLength(reader);
                	byte[] nowdata = new byte[size];
                	reader.read(nowdata, 0, size);
                	switch (typ) 
                	{
        				case "String":
        				{
        	            	Array.set(d, i, new String(nowdata, Charset.forName("UTF-8")));
        					break;
        				}
        			}
                }
            }
        }
        else
        {
        	Class nowtype = Class.forName("java.lang." + typ);
        	Method bufferget = ByteBuffer.class.getMethod("get" + typ);
            d = Array.newInstance(TypeFormat.TypeNameToType(typ), count);
            for (int i = 0; i < count; i++)
            {
            	byte[] readdata = new byte[nowtype.getField("BYTES").getInt(null)];
            	reader.read(readdata, 0, readdata.length);
            	readdata = TypeFormat.ArrayReverse(readdata);
            	Array.set(d, i, bufferget.invoke(ByteBuffer.wrap(readdata)));
            }
		}
        return d;
    }

    static Object GetTypNotArray(String typ, DataInputStream reader) throws Exception
    {
    	Object d = null;
        if (typ.equals("Byte") || typ.equals("Character") || typ.equals("Boolean"))
        {
        	Method bufferread = DataInputStream.class.getMethod("read" + TypeFormat.type3[Arrays.asList(TypeFormat.type).indexOf(typ)]);
            d = bufferread.invoke(reader);
        }
        else if(typ.equals("String"))
        {
        	int size = GetIntLength(reader);
        	byte[] nowdata = new byte[size];
        	reader.read(nowdata, 0, size);
        	switch (typ) 
        	{
				case "String":
				{
					d = new String(nowdata, Charset.forName("UTF-8"));
					break;
				}
			}
        }
        else if (typ.equals("Decimal")) {
        	byte[] nowdata = new byte[16];
        	reader.read(nowdata, 0, nowdata.length);
        	d = new Decimal(TypeFormat.ArrayReverse(nowdata));
		}
        else 
        {
        	Class nowtype = Class.forName("java.lang." + typ);
        	Method bufferget = ByteBuffer.class.getMethod("get" + typ);
        	byte[] readdata = new byte[nowtype.getField("BYTES").getInt(null)];
        	reader.read(readdata, 0, readdata.length);
        	readdata = TypeFormat.ArrayReverse(readdata);
        	d = bufferget.invoke(ByteBuffer.wrap(readdata));
		}
        return d;
    }
    
    public static Object GetTyp(DataInputStream reader) throws Exception
    {;
    
        byte data = reader.readByte();
        Object get;
        if (data < TypeFormat.type2.length)
        {
            String typ = TypeFormat.type2[data];
            
            if (typ.contains("[]"))
            {
                get = GetTypArray(typ, reader);
            }
            else if (typ.equals("Map"))
            {
                String[] typenames = new String[] { TypeFormat.type[reader.readByte()], TypeFormat.type[reader.readByte()] };
                Map d = new HashMap();
                int count = GetIntLength(reader);
                for (int ii = 0; ii < count; ii++)
                {
                    Object key = GetTyp(reader);
                    Object value = GetTyp(reader);
                    d.put(key, value);
                }
                get = d;
            }
            else if (typ.equals("null"))
            {
                boolean a = reader.readBoolean();
                get = null;
            }
            else if (Arrays.asList(TypeFormat.type2).indexOf(typ) != -1)
            {
                get = GetTypNotArray(typ, reader);
            }
            else
            {
                get = typ;
            }
        }
        else
        {
        	int size = GetIntLength(reader);
        	byte[] nowdata = new byte[size];
        	reader.read(nowdata, 0, size);
        	get = new String(nowdata, Charset.forName("UTF-8"));
        }
        return get;
    }

}
