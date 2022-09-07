package chuon;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.*;

class TypeFormat {
	public interface SerializationFunc
	{
		byte[] DataToBinary(Object data) throws Exception;
		Object BinaryToData(byte[] data, int[] index) throws Exception;
		String DataToString(Object data) throws Exception;
		Object StringToData(String data) throws Exception;
		String BinaryToString(byte[] data, int[] index) throws Exception;
		byte[] StringToBinary(String data) throws Exception;
	}
	
	private class NumSerializationFunc implements SerializationFunc
	{
		Class NumClass;
		String ByteToNumMethodName;
		String StringToNumMethodName;
		String GetByteMethodName;
		int Capacity;
		boolean BaseSupport = true;
		
		public NumSerializationFunc(Class NumClass, int Capacity, String byteToNumMethodName, String StringToNumMethodName, String GetByteMethodName, boolean BaseSupport) 
		{
			this.NumClass = NumClass;
			this.Capacity = Capacity;
			this.ByteToNumMethodName = byteToNumMethodName;
			this.StringToNumMethodName = StringToNumMethodName;
			this.GetByteMethodName = GetByteMethodName;
			this.BaseSupport = BaseSupport;
		}
		
		@Override
		public byte[] DataToBinary(Object data) throws Exception {
        	Method method = ByteBuffer.class.getMethod(GetByteMethodName, ToBuildinType.get(NumClass));
			return ((ByteBuffer)method.invoke(ByteBuffer.allocate(Capacity), data)).array();
		}
	
		@Override
		public String DataToString(Object data) throws Exception {
			return data.toString();
		}

		@Override
		public Object BinaryToData(byte[] data, int[] index) throws Exception {
        	Method method = ByteBuffer.class.getMethod(ByteToNumMethodName);
			Object ans = method.invoke(ByteBuffer.wrap(data, index[0], Capacity));
			index[0] += Capacity;
            return ans;
		}

		@Override
		public Object StringToData(String data) throws Exception {
			if(BaseSupport)
			{
	        	Method method = NumClass.getMethod(StringToNumMethodName, String.class, int.class);
				
	            data = StringTool.RemoveString(data, " ", "\n", "\r", "\t");
	            if (data.length() > 1 && data.charAt(0) == '0')
	            {
	                if (data.charAt(1) == 'b' || data.charAt(1) == 'B') return method.invoke(null, data, 2);
	                else if (data.charAt(1) == 'x' || data.charAt(1) == 'X') return method.invoke(null, data, 16);
	                else return method.invoke(null, data, 8);
	            }
	            else
	                return method.invoke(null, data, 10);
			}
			else {
	        	Method method = NumClass.getMethod(StringToNumMethodName, String.class);
	            data = StringTool.RemoveString(data, " ", "\n", "\r", "\t");
	            return method.invoke(null, data);
			}
		}
		
		@Override
		public String BinaryToString(byte[] data, int[] index) throws Exception {
			return DataToString(BinaryToData(data, index));
		}
		
		@Override
		public byte[] StringToBinary(String data) throws Exception {
			return DataToBinary(StringToData(data));
		}
	}
	
	public class typing
	{
        private byte index;
        private Class type;
        private String[] names;
        private boolean cannull;
        
        public SerializationFunc[] AllSerializationFunc;
        
        public byte getindex()
        {
        	return index;
        }
        public Class gettype()
        {
        	return type;
        }
        public String getname()
        {
        	return names[0];
        }
        public String[] getnames()
        {
        	return names.clone();
        }
        public boolean getcannull()
        {
        	return cannull;
        }
        
        public typing(byte index, Class type, String name, boolean cannull, SerializationFunc ... serializationFunc)
        {
            this.index = index;
            this.type = type;
            this.names = new String[] { name };
            this.cannull = cannull;
            if (serializationFunc.length == 0) throw new IllegalArgumentException("serializationFunc");
            this.AllSerializationFunc = serializationFunc;
        }
        
        public typing(byte index, Class type, String[] names, boolean cannull, SerializationFunc ... serializationFunc)
        {
            this.index = index;
            this.type = type;
            this.names = names.clone();
            this.cannull = cannull;
            if (serializationFunc.length == 0) throw new IllegalArgumentException("serializationFunc");
            this.AllSerializationFunc = serializationFunc;
        }
	}
	
	private ArrayList<typing> typings = new ArrayList<TypeFormat.typing>();

	public static final HashMap<Class, Class> ChangeType = new HashMap<Class, Class>()
	{
		{
			put(Byte.class, byte.class);
			put(Short.class, short.class);
			put(Integer.class, int.class);
			put(Long.class, long.class);
			put(Float.class, float.class);
			put(Double.class, double.class);
			put(Character.class, char.class);
			put(Boolean.class, boolean.class);
			put(byte.class, Byte.class);
			put(short.class, Short.class);
			put(int.class, Integer.class);
			put(long.class, Long.class);
			put(float.class, Float.class);
			put(double.class, Double.class);
			put(char.class, Character.class);
			put(boolean.class, Boolean.class);
		}
	};
	
	public static final HashMap<Class, Class> ToBuildinType = new HashMap<Class, Class>()
	{
		{
			put(Byte.class, byte.class);
			put(Short.class, short.class);
			put(Integer.class, int.class);
			put(Long.class, long.class);
			put(Float.class, float.class);
			put(Double.class, double.class);
			put(Character.class, char.class);
			put(Boolean.class, boolean.class);
			put(byte.class, byte.class);
			put(short.class, short.class);
			put(int.class, int.class);
			put(long.class, long.class);
			put(float.class, float.class);
			put(double.class, double.class);
			put(char.class, char.class);
			put(boolean.class, boolean.class);
		}
	};
	
	public static final HashMap<Class, Class> ToClassType = new HashMap<Class, Class>()
	{
		{
			put(Byte.class, Byte.class);
			put(Short.class, Short.class);
			put(Integer.class, Integer.class);
			put(Long.class, Long.class);
			put(Float.class, Float.class);
			put(Double.class, Double.class);
			put(Character.class, Character.class);
			put(Boolean.class, Boolean.class);
			put(byte.class, Byte.class);
			put(short.class, Short.class);
			put(int.class, Integer.class);
			put(long.class, Long.class);
			put(float.class, Float.class);
			put(double.class, Double.class);
			put(char.class, Character.class);
			put(boolean.class, Boolean.class);
		}
	};

	private static TypeFormat _instance;
	
    public static TypeFormat instance()
    {
            if (_instance == null) _instance = new TypeFormat();
            return _instance;
    }
	
    public typing get(int index) 
    {
    	for (int i = 0; i < typings.size(); i++)
        {
            if (typings.get(i).index == index) return typings.get(i);
        }
        throw new ArrayIndexOutOfBoundsException();
	}
	
    public typing get(Class index) 
    {
    	if(ToBuildinType.containsKey(index)) index = ToBuildinType.get(index);
        for (int i = 0; i < typings.size(); i++)
        {
            if (typings.get(i).type == index) return typings.get(i);
        }
        Class[] allinterfaces = index.getInterfaces();
        for (Class nowtype : allinterfaces)
        {
            for (int i = 0; i < typings.size(); i++)
            {
                if (typings.get(i).type == nowtype) return typings.get(i);
            }
        }
        return get(index.getSuperclass());
	}
    
    public typing get(String index)
    {
        for (int i = 0; i < typings.size(); i++)
        {
            if (Arrays.asList(typings.get(i).getnames()).contains(index)) return typings.get(i);
        }
        throw new ArrayIndexOutOfBoundsException();
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
    
    public static int GetIntLength(byte[] data, int[] index) throws IOException
    {
    	ArrayList<Integer> vs = new ArrayList<Integer>();
    	int a;
        do
        {
        	a = data[index[0]] & 0x0FF;
            vs.add(a % 128);
            index[0]++;
        } while (a >= 128);
        int x = 0;
        for (int i = 0; i < vs.size(); i++)
        {
            x += (int)(vs.get(i) * Math.pow(128, i));
        }
        return x;
    }
    
    static int ArrayRank(Class[] basetype, Object thing)
    {
        if (thing == null)
        {
            basetype[0] = Object.class;
            return 0;
        }
        int rank = 0;
        for (basetype[0] = thing.getClass(); basetype[0].getComponentType() != null; basetype[0] = basetype[0].getComponentType())
        {
            if (basetype[0].isArray())
            {
                rank += 1;
            }
        }
        return rank;
    }
    
    static int ArrayRank(Class[] basetype, Class nowtype)
    {
    	int rank = 0;
        for (basetype[0] = nowtype; basetype[0].getComponentType() != null; basetype[0] = basetype[0].getComponentType())
        {
            if (basetype[0].isArray())
            {
            	rank += 1;
            }
        }
        return rank;
    }
	
	public static Object PrimitiveAndClassArray(Object inputArray, Map<Class, Class> Maplist)
    {
    	if(inputArray == null || !inputArray.getClass().isArray())
    	{
    		return inputArray;
    	}
    	
    	Class[] basetype = new Class[1];
    	int rank = ArrayRank(basetype, inputArray);
    	
    	Object output = inputArray;
    	if(Maplist.containsKey(basetype[0]))
    	{
    		Class newtype = Maplist.get(basetype[0]);
    		if(basetype[0] == newtype) return inputArray;
            for (int i = 0; i < rank - 1; i++)
            {
            	newtype = Array.newInstance(newtype, 0).getClass();
            }
	    	int len = Array.getLength(inputArray);
	    	output = Array.newInstance(newtype, len);
	    	for(int i = 0; i < len; i++)
	    	{
	    		Array.set(output, i, PrimitiveAndClassArray(Array.get(inputArray, i), Maplist));
	    	}
    	}
    	return output;
    }
    public static Object ArrayReverse(Object input)
    {
    	if(!input.getClass().isArray())
    	{
    		return input;
    	}
    	int len = Array.getLength(input);
    	Object output = Array.newInstance(input.getClass().getComponentType(), len);
		for(int i = 0; i < len; i++)
		{
			Array.set(output, len - i - 1, Array.get(input, i));
		}
		return output;
	}
    
    private TypeFormat()
    {    	
    	// object
        typings.add(new typing((byte)0, Object.class, "object", false,
    		new SerializationFunc() {

				@Override
				public byte[] DataToBinary(Object data) throws Exception {
                    if (data == null)
                        return new byte[] { 0, 0 };
                    ChuonBinary chuonBinary = new ChuonBinary(data);
                    return chuonBinary.toArray();
				}
			
				@Override
				public String DataToString(Object data) throws Exception {
                    if (data == null) return "null";
                    ChuonString chuonString = new ChuonString(data);
                    return chuonString.toString();
				}

				@Override
				public Object BinaryToData(byte[] data, int[] index) throws Exception {
                    if (data[index[0]] == 0 && data[index[0] + 1] == 0)
                    {
                        index[0] += 2;
                        return null;
                    }
                    ChuonBinary chuonBinary = new ChuonBinary(data, index);
                    return chuonBinary.toObject();
				}

				@Override
				public Object StringToData(String data) throws Exception {
                    if (StringTool.RemoveString(data, " ", "\n", "\r", "\t").equals("null")) return null;
                    ChuonString chuonString = new ChuonString(data);
                    return chuonString.toObject();
				}
				
				@Override
				public String BinaryToString(byte[] data, int[] index) throws Exception {
					return DataToString(BinaryToData(data, index));
				}
				
				@Override
				public byte[] StringToBinary(String data) throws Exception {
					return DataToBinary(StringToData(data));
				}
			}
        ));
        
        // byte
        typings.add(new typing((byte)1, byte.class, "byte", false,
       	    new SerializationFunc() {
   				@Override
   				public byte[] DataToBinary(Object data) throws Exception {
   					return new byte[] { (byte)data };
   				}
   			
   				@Override
   				public String DataToString(Object data) throws Exception {
   					return ((Integer)Integer.parseInt(String.format("%02X", (byte)data),16)).toString();
   				}

   				@Override
   				public Object BinaryToData(byte[] data, int[] index) throws Exception {
   					return data[index[0]++];
   				}

   				@Override
   				public Object StringToData(String data) throws Exception {
                    if (data.length() > 1 && data.charAt(0) == '0')
                    {
                        if (data.charAt(1) == 'b' || data.charAt(1) == 'B') return Byte.parseByte(data, 2);
                        else if (data.charAt(1) == 'x' || data.charAt(1) == 'X') return Byte.parseByte(data, 16);
                        else return Byte.parseByte(data, 8);
                    }
                    else
                        return Byte.parseByte(data);
   				}
   				
   				@Override
   				public String BinaryToString(byte[] data, int[] index) throws Exception {
   					return DataToString(BinaryToData(data, index));
   				}
   				
   				@Override
   				public byte[] StringToBinary(String data) throws Exception {
   					return DataToBinary(StringToData(data));
   				}
   			},
    	    new SerializationFunc() {

				@Override
				public byte[] DataToBinary(Object data) throws Exception {
                    byte[] len = GetBytesLength(((byte[])data).length);
                    byte[] ans = Arrays.copyOf(len, len.length + ((byte[])data).length);
                    System.arraycopy((byte[])data, 0, ans, len.length, ((byte[])data).length);
                    return ans;
				}
			
				@Override
				public String DataToString(Object data) throws Exception {
					return StringTool.BytesToHex((byte[])data);
				}

				@Override
				public Object BinaryToData(byte[] data, int[] index) throws Exception {
                    int len = GetIntLength(data, index);
                    byte[] nowdata = new byte[len];
                    System.arraycopy(data, index[0], nowdata, 0, len);
                    index[0] += len;
                    return nowdata;
				}

				@Override
				public Object StringToData(String data) throws Exception {
					return StringTool.HexToBytes(StringTool.RemoveString(data, " "));
				}
				
				@Override
				public String BinaryToString(byte[] data, int[] index) throws Exception {
                    int len = GetIntLength(data, index);
                    byte[] nowdata = new byte[len];
                    System.arraycopy(data, index[0], nowdata, 0, len);
                    index[0] += len;
                    return StringTool.BytesToHex(nowdata);
				}
				
				@Override
				public byte[] StringToBinary(String data) throws Exception {
                    byte[] newdata = StringTool.HexToBytes(StringTool.RemoveString(data, " "));
                    byte[] len = GetBytesLength(newdata.length);
                    byte[] ans = Arrays.copyOf(len, len.length + newdata.length);
                    System.arraycopy(newdata, 0, ans, len.length, newdata.length);
                    return ans;
				}
			}
        ));
        
        // short
        typings.add(new typing((byte)2, short.class, "short", false, new NumSerializationFunc(Short.class, 2, "getShort", "parseShort", "putShort", true)));
        
        // int
        typings.add(new typing((byte)3, int.class, "int", false, new NumSerializationFunc(Integer.class, 4, "getInt", "parseInt", "putInt", true)));

        // long
        typings.add(new typing((byte)4, long.class, "long", false, new NumSerializationFunc(Long.class, 8, "getLong", "parseLong", "putLong", true)));
        
        // ushort
        typings.add(new typing((byte)5, short.class, "ushort", false, new NumSerializationFunc(Short.class, 2, "getShort", "parseShort", "putShort", true)));
        
        // uint
        typings.add(new typing((byte)6, int.class, "uint", false, new NumSerializationFunc(Integer.class, 4, "getInt", "parseInt", "putInt", true)));

        // ulong
        typings.add(new typing((byte)7, long.class, "ulong", false, new NumSerializationFunc(Long.class, 8, "getLong", "parseLong", "putLong", true)));
        
        // float
        typings.add(new typing((byte)8, float.class, "float", false, new NumSerializationFunc(Float.class, 4, "getFloat", "parseFloat", "putFloat", false)));
        
        // double
        typings.add(new typing((byte)9, double.class, "double", false, new NumSerializationFunc(Double.class, 8, "getDouble", "parseDouble", "putDouble", false)));
        
        // bool
        typings.add(new typing((byte)10, boolean.class, "bool", false,
       	    new SerializationFunc() {

   				@Override
   				public byte[] DataToBinary(Object data) throws Exception {
   					return ((boolean)data ? new byte[] { 1 } : new byte[] { 0 });
   				}
   			
   				@Override
   				public String DataToString(Object data) throws Exception {
   					return ((Boolean)data).toString().toLowerCase();
   				}

   				@Override
   				public Object BinaryToData(byte[] data, int[] index) throws Exception {
                    byte temp = data[index[0]];
                    index[0]++;
                    return temp != 0;
   				}

   				@Override
   				public Object StringToData(String data) throws Exception {
   					return Boolean.parseBoolean(StringTool.RemoveString(data, " ", "\n", "\r", "\t"));
   				}
   				
   				@Override
   				public String BinaryToString(byte[] data, int[] index) throws Exception {
   					return DataToString(BinaryToData(data, index));
   				}
   				
   				@Override
   				public byte[] StringToBinary(String data) throws Exception {
   					return DataToBinary(StringToData(data));
   				}
   			},
       	    new SerializationFunc() {

   				@Override
   				public byte[] DataToBinary(Object data) throws Exception {
                    byte[] len = GetBytesLength(((boolean[])data).length);
                    if (((boolean[])data).length == 0) return len;
                    byte[] output = Arrays.copyOf(len, len.length + (((boolean[])data).length - 1) / 8 + 1);
                    for (int i = 0, k = len.length - 1; i < ((boolean[])data).length; i++)
                    {
                        if (i % 8 == 0)
                        {
                            k++;
                            output[k] = 0;
                        }
                        output[k] <<= 1;
                        if (((boolean[])data)[i])
                        {
                            output[k]++;
                        }
                    }
                    return output;
   				}
   			
   				@Override
   				public String DataToString(Object data) throws Exception {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < ((boolean[])data).length; i++)
                    {
                        stringBuilder.append(((boolean[])data)[i] ? "1" : "0");
                    }
                    return stringBuilder.toString();
   				}

   				@Override
   				public Object BinaryToData(byte[] data, int[] index) throws Exception {
                    int len = GetIntLength(data, index);
                    boolean[] output = new boolean[len];
                    byte nowbools = 0;
                    for (int i = 0; i < len; i++)
                    {
                        if (i % 8 == 0)
                        {
                            nowbools = data[index[0]];
                            index[0]++;
                        }
                        int nowleft = len - (i / 8 * 8) >= 8 ? 8 : len - (i / 8 * 8);
                        if ((i / 8 * 8) + (nowleft - 1 - (i % 8)) < output.length)
                        {
                            output[(i / 8 * 8) + (nowleft - 1 - (i % 8))] = (nowbools & 1) == 1;
                        }
                        nowbools >>= 1;
                    }
                    return output;
   				}

   				@Override
   				public Object StringToData(String data) throws Exception {
                    data = StringTool.RemoveString(data, " ", "\n", "\r", "\t");
                    boolean[] output = new boolean[data.length()];
                    for (int i = 0; i < data.length(); i++)
                    {
                        output[i] = Byte.parseByte(((Character)data.charAt(i)).toString()) != 0;
                    }
                    return output;
   				}
   				
   				@Override
   				public String BinaryToString(byte[] data, int[] index) throws Exception {
   					return DataToString(BinaryToData(data, index));
   				}
   				
   				@Override
   				public byte[] StringToBinary(String data) throws Exception {
   					return DataToBinary(StringToData(data));
   				}
   			}
        ));
        
        // char
        typings.add(new typing((byte)11, char.class, "char", false,
       	     new SerializationFunc() {

   				@Override
   				public byte[] DataToBinary(Object data) throws Exception {
   					return data.toString().getBytes("UTF8");
   				}
   			
   				@Override
   				public String DataToString(Object data) throws Exception {
   					return "\'" + StringTool.Escape(data.toString()) + "\'";
   				}

   				@Override
   				public Object BinaryToData(byte[] data, int[] index) throws Exception 
   				{
   					String getdata = null;
   					for(int i = 1; (getdata == null || getdata.charAt(0) == 65533) && i <= data.length; i++)
   					{
   						getdata = new String(Arrays.copyOfRange(data, index[0], index[0] + i), "UTF8");
   					}
   					index[0] += ((Character)getdata.charAt(0)).toString().getBytes("UTF8").length;
                    return getdata.charAt(0);
   				}

   				@Override
   				public Object StringToData(String data) throws Exception {
   					return StringTool.Unescape(StringTool.TakeString(data, '\'', '\'')[0]).charAt(0);
   				}
   				
   				@Override
   				public String BinaryToString(byte[] data, int[] index) throws Exception {
   					return DataToString(BinaryToData(data, index));
   				}
   				
   				@Override
   				public byte[] StringToBinary(String data) throws Exception {
   					return DataToBinary(StringToData(data));
   				}
   			}
        ));
        
        // string
        typings.add(new typing((byte)12, String.class, "string", true,
          	 new SerializationFunc() {
  				@Override
  				public byte[] DataToBinary(Object data) throws Exception {
  					byte[] strdata = data.toString().getBytes("UTF8");
  					byte[] strlen = GetBytesLength(strdata.length);
                    byte[] ans = Arrays.copyOf(strlen, strlen.length + ((byte[])strdata).length);
                    System.arraycopy((byte[])strdata, 0, ans, strlen.length, ((byte[])strdata).length);
  					return ans;
  				}
  			
  				@Override
  				public String DataToString(Object data) throws Exception {
  					return "\"" + StringTool.Escape(data.toString()) + "\"";
  				}

  				@Override
  				public Object BinaryToData(byte[] data, int[] index) throws Exception 
  				{
                    int len = GetIntLength(data, index);
                    byte[] nowdata = new byte[len];
                    System.arraycopy(data, index[0], nowdata, 0, len);
                    index[0] += len;
                    return new String(nowdata, "UTF8");
  				}

  				@Override
  				public Object StringToData(String data) throws Exception {
  					return StringTool.Unescape(StringTool.TakeString(data, '\"', '\"')[0]);
  				}
  				
  				@Override
  				public String BinaryToString(byte[] data, int[] index) throws Exception {
  					return DataToString(BinaryToData(data, index));
  				}
  				
  				@Override
  				public byte[] StringToBinary(String data) throws Exception {
  					return DataToBinary(StringToData(data));
  				}
  			}
        ));
        
        typings.add(new typing((byte)13, Map.class, new String[] {"dict", "map", "Map", "Dictionary"}, true,
       	     new SerializationFunc() {

   				@Override
   				public byte[] DataToBinary(Object data) throws Exception {
   					Map nowdata = (Map)data;
   	                Class datatype = data.getClass();
   	                Object[][] keysandvalues = new Object[][] {nowdata.keySet().toArray(), nowdata.values().toArray()} ;
   	                Class[] Subdatatype = new Class[2];
   	                int[] keysandvaluesrank = new int[2];
   	                for(int i = 0; i < keysandvalues.length; i++)
   	                {
   	                    for(int j = 0; j < keysandvalues[i].length; j++)
   	                    {
   	                    	Class[] basetype = new Class[1];
   	                    	keysandvalues[i][j] = PrimitiveAndClassArray(keysandvalues[i][j], ToBuildinType);
   	                    	keysandvaluesrank[i] = ArrayRank(basetype, keysandvalues[i][j] == null ? Object.class : keysandvalues[i][j].getClass());
   	                    	Class nowclass = get(basetype[0]).type;
   	                    	for (int k = 0; k < keysandvaluesrank[i]; k++)
	   	                    {
   	                    		nowclass = Array.newInstance(nowclass, 0).getClass();
	   	                    }
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
   	                Class[][] keysandvaluebasetype = new Class[2][1];
                   	keysandvaluesrank[0] = ArrayRank(keysandvaluebasetype[0], Subdatatype[0]);
                   	keysandvaluesrank[1] = ArrayRank(keysandvaluebasetype[1], Subdatatype[1]);
                   	typing[] keysandvaluetyping = new typing[] {get(keysandvaluebasetype[0][0]), get(keysandvaluebasetype[1][0])};
                   	try(ByteArrayOutputStream stream = new ByteArrayOutputStream())
                   	{
                   		try(DataOutputStream writer = new DataOutputStream(stream))
                   		{
                            writer.writeByte(keysandvaluetyping[0].index);
                            writer.writeByte((byte)keysandvaluesrank[0]);
                            writer.writeByte(keysandvaluetyping[1].index);
                            writer.writeByte((byte)keysandvaluesrank[1]);
                            writer.write(GetBytesLength(nowdata.size()));
                            for (int i = 0; i < nowdata.size(); i++)
                            {
                                Object key = keysandvalues[0][i];
                                Object value = keysandvalues[1][i];
                                writer.write(ChuonBinary.Typing(keysandvaluetyping[0], key, keysandvaluesrank[0]));
                                writer.write(ChuonBinary.Typing(keysandvaluetyping[1], value, keysandvaluesrank[1]));
                            }
                            writer.close();
                            stream.close();
                            return stream.toByteArray();
                   		}
                   	}
   				}
   			
   				@Override
   				public String DataToString(Object data) throws Exception {
   					Map nowdata = (Map)data;
   	                Class datatype = data.getClass();
   	                Object[][] keysandvalues = new Object[][] {nowdata.keySet().toArray(), nowdata.values().toArray()} ;
   	                Class[] Subdatatype = new Class[2];
   	                int[] keysandvaluesrank = new int[2];
   	                for(int i = 0; i < keysandvalues.length; i++)
   	                {
   	                    for(int j = 0; j < keysandvalues[i].length; j++)
   	                    {
   	                    	Class[] basetype = new Class[1];
   	                    	keysandvalues[i][j] = PrimitiveAndClassArray(keysandvalues[i][j], ToBuildinType);
   	                    	keysandvaluesrank[i] = ArrayRank(basetype, keysandvalues[i][j] == null ? Object.class : keysandvalues[i][j].getClass());
   	                    	Class nowclass = get(basetype[0]).type;
   	                    	for (int k = 0; k < keysandvaluesrank[i]; k++)
	   	                    {
   	                    		nowclass = Array.newInstance(nowclass, 0).getClass();
	   	                    }
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
   	                Class[][] keysandvaluebasetype = new Class[2][1];
                   	keysandvaluesrank[0] = ArrayRank(keysandvaluebasetype[0], Subdatatype[0]);
                   	keysandvaluesrank[1] = ArrayRank(keysandvaluebasetype[1], Subdatatype[1]);
                   	typing[] keysandvaluetyping = new typing[] {get(keysandvaluebasetype[0][0]), get(keysandvaluebasetype[1][0])};

                   	StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("{");
                    stringBuilder.append(keysandvaluetyping[0].getname());
                    for (int i = 0; i < keysandvaluesrank[0]; i++)
                    {
                        stringBuilder.append("[]");
                    }
                    stringBuilder.append(":");
                    stringBuilder.append(keysandvaluetyping[1].getname());
                    for (int i = 0; i < keysandvaluesrank[1]; i++)
                    {
                        stringBuilder.append("[]");
                    }
                    stringBuilder.append(":");
                    for (int i = 0; i < nowdata.size(); i++)
                    {
                        stringBuilder.append("{");
                        Object key = keysandvalues[0][i];
                        Object value = keysandvalues[1][i];
                        stringBuilder.append(ChuonString.Typing(keysandvaluetyping[0], key, keysandvaluesrank[0]));
                        stringBuilder.append(",");
                        stringBuilder.append(ChuonString.Typing(keysandvaluetyping[1], value, keysandvaluesrank[1]));
                        stringBuilder.append("}");
                    }
                    stringBuilder.append("}");
                    return stringBuilder.toString();
   				}

   				@Override
   				public Object BinaryToData(byte[] data, int[] index) throws Exception {
                    typing keytyping = get(data[index[0]++]);
                    int keyrank = data[index[0]++];
                    typing datatyping = get(data[index[0]++]);
                    int datarank = data[index[0]++];
                    int len = GetIntLength(data, index);

                    Class keynowtype = keytyping.type;
                    for (int i = 0; i < keyrank; i++)
                    {
                    	keynowtype = Array.newInstance(keynowtype, 0).getClass();
                    }
                    Class datanowtype = datatyping.type;
                    for (int i = 0; i < datarank; i++)
                    {
                    	keynowtype = Array.newInstance(datanowtype, 0).getClass();
                    }
                    
                    HashMap ans = new HashMap();
                    for (int i = 0; i < len; i++)
                    {
                        Object key = ChuonBinary.GetTyp(keytyping, data, keyrank, index);
                        Object value = ChuonBinary.GetTyp(datatyping, data, datarank, index);
                        ans.put(key, value);
                    }
   					return ans;
   				}

   				@Override
   				public Object StringToData(String data) throws Exception {
                    String[] typeanddata = StringTool.SplitWithFormatWithoutinArray(StringTool.TakeString(data, '{', '}')[0], ':');
                    typeanddata[0] = StringTool.RemoveString(typeanddata[0], " ", "\n", "\r", "\t");
                    typeanddata[1] = StringTool.RemoveString(typeanddata[1], " ", "\n", "\r", "\t");
                    typing keytyping = get(StringTool.RemoveString(typeanddata[0], "[]"));
                    int keyrank = StringTool.TakeString(typeanddata[0], '[', ']').length;
                    typing datatyping = get(StringTool.RemoveString(typeanddata[1], "[]"));
                    int datarank = StringTool.TakeString(typeanddata[1], '[', ']').length;
                    String[] alldata = StringTool.TakeString(typeanddata[2], '{', '}');
                    int len = alldata.length;
                    
                    Class keynowtype = keytyping.type;
                    for (int i = 0; i < keyrank; i++)
                    {
                    	keynowtype = Array.newInstance(keynowtype, 0).getClass();
                    }
                    Class datanowtype = datatyping.type;
                    for (int i = 0; i < datarank; i++)
                    {
                    	keynowtype = Array.newInstance(datanowtype, 0).getClass();
                    }
                    
                    HashMap ans = new HashMap();
                    for (int i = 0; i < len; i++)
                    {
                        String[] nowdata = StringTool.SplitWithFormatWithoutinArray(alldata[i], ',');
                        if (keyrank > 0)
                        {
                            nowdata[0] = StringTool.TakeString(nowdata[0], '{', '}')[0];
                        }
                        Object key = ChuonString.GetTyp(keytyping, keyrank, nowdata[0]);
                        if (datarank > 0)
                        {
                            nowdata[1] = StringTool.TakeString(nowdata[1], '{', '}')[0];
                        }
                        Object value = ChuonString.GetTyp(datatyping, datarank, nowdata[1]);
                        ans.put(key, value);
                    }
                    return ans;
   				}
   				
   				@Override
   				public String BinaryToString(byte[] data, int[] index) throws Exception {
   					return DataToString(BinaryToData(data, index));
   				}
   				
   				@Override
   				public byte[] StringToBinary(String data) throws Exception {
   					return DataToBinary(StringToData(data));
   				}
   			}
       	));
    }
}
