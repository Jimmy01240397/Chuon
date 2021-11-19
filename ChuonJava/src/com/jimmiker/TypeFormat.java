package com.jimmiker;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

class TypeFormat {
	public static final String[] type = new String[] { "Byte[]", "SByte[]", "Short[]", "Integer[]", "Long[]", "UShort[]", "UInteger[]", "ULong[]", "Float[]", "Double[]", "Decimal[]", "Character[]", "String[]", "Boolean[]", "Object[]", "Map", "Byte", "SByte", "Short", "Integer", "Long", "UShort", "UInteger", "ULong", "Float", "Double", "Decimal", "Character", "String", "Boolean", "Object", "null" };
	public static final String[] type2 = new String[] { "Byte[]", "Byte[]", "Short[]", "Integer[]", "Long[]", "Short[]", "Integer[]", "Long[]", "Float[]", "Double[]", "Decimal[]", "Character[]", "String[]", "Boolean[]", "Object[]", "Map", "Byte", "Byte", "Short", "Integer", "Long", "Short", "Integer", "Long", "Float", "Double", "Decimal", "Character", "String", "Boolean", "Object", "null" };
	public static final String[] type3 = new String[] { "Byte[]", "SByte[]", "Short[]", "Integer[]", "Long[]", "UShort[]", "UInteger[]", "ULong[]", "Float[]", "Double[]", "Decimal[]", "Char[]", "String[]", "Boolean[]", "Object[]", "Map", "Byte", "SByte", "Short", "Integer", "Long", "UShort", "UInteger", "ULong", "Float", "Double", "Decimal", "Char", "String", "Boolean", "Object", "null" };
	public static final String[] typelist = new String[] { "byte[]", "sbyte[]", "short[]", "int[]", "long[]", "ushort[]", "uint[]", "ulong[]", "float[]", "double[]", "decimal[]", "char[]", "string[]", "bool[]", "object[]", "Dictionary", "byte", "sbyte", "short", "int", "long", "ushort", "uint", "ulong", "float", "double", "decimal", "char", "string", "bool", "object" };
	public static final String[] typelist2 = new String[] { "Byte[]", "SByte[]", "Short[]", "Integer[]", "Long[]", "UShort[]", "UInteger[]", "ULong[]", "Float[]", "Double[]", "Decimal[]", "Character[]", "String[]", "Boolean[]", "Object[]", "Map", "Byte", "SByte", "Short", "Integer", "Long", "UShort", "UInteger", "ULong", "Float", "Double", "Decimal", "Character", "String", "Boolean", "Object" };
	public static final String[] typelist3 = new String[] { "Byte[]", "Byte[]", "Short[]", "Integer[]", "Long[]", "Short[]", "Integer[]", "Long[]", "Float[]", "Double[]", "Decimal[]", "Character[]", "String[]", "Boolean[]", "Object[]", "Map", "Byte", "Byte", "Short", "Integer", "Long", "Short", "Integer", "Long", "Float", "Double", "Decimal", "Character", "String", "Boolean", "Object" };
	public static final String[] typelist4 = new String[] { "Byte[]", "Short[]", "Short[]", "Integer[]", "Long[]", "Integer[]", "Long[]", "BigInteger[]", "Float[]", "Double[]", "Decimal[]", "Character[]", "String[]", "Boolean[]", "Object[]", "Map", "Byte", "Short", "Short", "Integer", "Long", "Integer", "Long", "BigInteger", "Float", "Double", "Decimal", "Character", "String", "Boolean", "Object" };
	public static final HashMap<String, Class> ChangeType = new HashMap<String, Class>()
	{
		{
			put("Byte", byte.class);
			put("Byte[]", byte[].class);
			put("Short", short.class);
			put("Integer", int.class);
			put("Long", long.class);
			put("Float", float.class);
			put("Double", double.class);
			put("Character", char.class);
			put("Boolean", boolean.class);
			put("byte", Byte.class);
			put("byte[]", Byte[].class);
			put("short", Short.class);
			put("int", Integer.class);
			put("long", Long.class);
			put("float", Float.class);
			put("double", Double.class);
			put("char", Character.class);
			put("boolean", Boolean.class);
		}
	};

    public static String ToTrueTypeName(String type)
    {
        String typenames = type;
        if (Arrays.asList(typelist2).indexOf(type) == -1 && !type.equals("null"))
        {
            typenames = typelist2[Arrays.asList(typelist).indexOf(type)];
        }
        return typenames;
    }
    
    public static String ToJavaTrueTypeName(String type)
    {
        String typenames = type;
        if (Arrays.asList(typelist3).indexOf(type) == -1 && !type.equals("null"))
        {
            typenames = typelist3[Arrays.asList(typelist).indexOf(type)];
        }
        return typenames;
    }
    
    public static String ToJavaScannrTrueTypeName(String type)
    {
        String typenames = type;
        if (Arrays.asList(typelist4).indexOf(type) == -1 && !type.equals("null"))
        {
            typenames = typelist4[Arrays.asList(typelist).indexOf(type)];
        }
        return typenames;
    }

    public static String ToSimpleTypeName(String type)
    {
        String typenames = type;
        if (Arrays.asList(typelist).indexOf(type) == -1 && !type.equals("null"))
        {
            typenames = typelist[Arrays.asList(typelist2).indexOf(type)];
        }
        return typenames;
    }

    public static Class TypeNameToType(String typename) throws ClassNotFoundException
    {
        return Class.forName((typename.equals("Map") ? "java.util.Hash" : (typename.equals("Decimal") ? "com.jimmiker." : "java.lang.")) + typename);
    }

	public static Object PrimitiveAndClassArray(Object inputArray)
    {
    	if(!inputArray.getClass().isArray())
    	{
    		return inputArray;
    	}
    	String name = inputArray.getClass().getSimpleName().replace("[]", "");
    	Object output = inputArray;
    	if(ChangeType.containsKey(name))
    	{
    		if(inputArray.getClass().getSimpleName().replace(name, "").equals("[][]"))
    		{
    			name = name += "[]";
    		}
    		
	    	int len = Array.getLength(inputArray);
	    	output = Array.newInstance(ChangeType.get(name), len);
	    	for(int i = 0; i < len; i++)
	    	{
	    		Array.set(output, i, PrimitiveAndClassArray(Array.get(inputArray, i)));
	    	}
    	}
    	//System.out.println("ggg " + now.getClass().getSimpleName());
    	return output;
    }
    public static byte[] ArrayReverse(byte[] input)
    {
    	byte[] output = new byte[input.length];
		for(int i = 0; i < input.length; i++)
		{
			output[input.length - i - 1] = input[i];
		}
		return output;
	}
}
