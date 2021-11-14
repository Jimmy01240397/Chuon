package com.jimmiker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ChuonString {
    String data = "";
    public ChuonString(Object thing) throws Exception
    {
        data = ObjectToString(0, thing, false);
    }
    
    public ChuonString(String thing) throws Exception
    {
        data = thing;
        toObject();
    }
    
    public ChuonString(byte[] thing, Charset encoding) throws Exception
    {
        data = new String(thing, encoding);
        toObject();
    }

    public Object toObject() throws Exception
    {
        return ChuonStringDeserializeToObject(data);
    }

    @Override
    public String toString()
    {
        return data;
    }

    public byte[] toBinaryArray(Charset encoding) throws UnsupportedEncodingException 
    {
		return data.getBytes(encoding);
	}
    
    public String toStringWithEnter() throws Exception
    {
        return ObjectSerializeToChuonStringWithEnter(toObject());
    }

    public ChuonBinary toChuonBinary() throws Exception
    {
        Object datas = toObject();
        return new ChuonBinary(datas);
    }

    static String ObjectToStringForArray(int cont, Object thing, boolean enter) throws Exception
    {
        String type = TypeFormat.ToSimpleTypeName(thing.getClass().getSimpleName());
        String a = "";
        if (Array.getLength(thing) > 0)
        {
            a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
            if (type == "byte[]")
            {
                a += StringTool.BytesToHex((byte[])thing) + printTab(enter, cont) + "}";
            }
            else
            {
            	Func<Object, String> makestring = new Func<Object, String>() {
					@Override
					public String run(Object... args) {
	                    if ((boolean)args[1])
	                        return (String)args[0] + BeforeFormatString(Array.get(thing, (int)args[2]).toString(), new char[] { '\'', '\"', '{', '}', '[', ']', ',', ':' }) + (String)args[0];
	                    else
		                    return Array.get(thing, (int)args[2]).toString();
					}
				};
                for (int i = 0; i < Array.getLength(thing) - 1; i++)
                {
                    if (type == "object[]")
                        a += ObjectToString(cont + 1, Array.get(thing, i), enter) + "," + printTab(enter, cont + 1);
                    else
                        a += makestring.run(type == "char[]" ? "\'" : "\"", type == "char[]" || type == "string[]", i) + "," + printTab(enter, cont + 1);
                }
                if (type == "object[]")
                    a += ObjectToString(cont + 1, Array.get(thing, Array.getLength(thing) - 1), enter) + printTab(enter, cont) + "}";
                else
                    a += makestring.run(type == "char[]" ? "\'" : "\"", type == "char[]" || type == "string[]", Array.getLength(thing) - 1) + printTab(enter, cont) + "}";
            }
        }
        else
        {
            a += "NotThing";
        }
        return a;
    }

    static String ObjectToString(int cont, Object thing, boolean enter) throws Exception
    {
        String a = "";
        if (thing != null)
        {
        	thing = thing.getClass().getName().length() > 2 ? thing : TypeFormat.PrimitiveAndClassArray(thing);
        	String typename = Arrays.asList(TypeFormat.type).indexOf(thing.getClass().getSimpleName()) >= 0 ? thing.getClass().getSimpleName() : thing.getClass().getInterfaces()[0].getSimpleName();

            String typ = TypeFormat.typelist[Arrays.asList(TypeFormat.typelist2).indexOf(typename)];
            a += typ + ":";
            if (typ.contains("[]"))
            {
                a += ObjectToStringForArray(cont, thing, enter);
            }
            else if (typ == "Dictionary")
            {
                Map c = (Map)thing;
                Class datatype = thing.getClass();
                Object[][] data = new Object[][] {c.keySet().toArray(), c.values().toArray()} ;
                Class[] Subdatatype = new Class[2];
                for(int i = 0; i < data.length; i++)
                {
                    for(int j = 0; j < data[i].length; j++)
                    {
                    	Class nowclass = Arrays.asList(TypeFormat.typelist2).indexOf(data[i][j].getClass().getSimpleName()) >= 0 ? data[i][j].getClass() : data[i][j].getClass().getInterfaces()[0];
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
            	
                a += printTab(enter, cont) + "{" + printTab(enter, cont + 1) + TypeFormat.typelist[Arrays.asList(TypeFormat.typelist2).indexOf(Subdatatype[0].getSimpleName())] + ":" + TypeFormat.typelist[Arrays.asList(TypeFormat.typelist2).indexOf(Subdatatype[1].getSimpleName())] + ":";

                if (c.size() > 0)
                {
                    for (int i = 0; i < c.size(); i++)
                    {
                        a += printTab(enter, cont + 1) + "{" + printTab(enter, cont + 2) + ObjectToString(cont + 2, data[0][i], enter) + "," + printTab(enter, cont + 2) + ObjectToString(cont + 2, data[1][i], enter) + printTab(enter, cont + 1) + "}";
                    }
                }
                else
                {
                    a += "NotThing";
                }
                a += printTab(enter, cont) + "}";
            }
            else if (Arrays.asList(TypeFormat.typelist).indexOf(typ) != -1)
            {
            	Object nowthing = thing; 
            	Func<Object, String> makestring = new Func<Object, String>() {
					
					@Override
					public String run(Object... args) {
	                    if ((boolean)args[1])
	                        return (String)args[0] + BeforeFormatString(nowthing.toString(), new char[] { '\'', '\"', '{', '}', '[', ']', ',', ':' }) + (String)args[0];
	                    else
	                        return nowthing.toString();
					}
				};
                a += makestring.run(typ == "char" ? "\'" : "\"", typ == "char" || typ == "string");
            }
            else
            {
                a += thing.toString();
            }
        }
        else
        {
            a += "null";
        }
        return a;
    }

    static String ObjectSerializeToChuonString(Object thing) throws Exception
    {
        return ObjectToString(0, thing, false);
    }

    static String ObjectSerializeToChuonStringWithEnter(Object thing) throws Exception
    {
        return ObjectToString(0, thing, true);
    }

    static Object StringToObjectForArray(String thing) throws Exception
    {
        String[] vs = StringTool.SplitWithFormat(thing, ':');
        String typ =StringTool.RemoveString(vs[0], " ", "\n", "\r", "\t", "[", "]");

        String typenames = TypeFormat.ToJavaTrueTypeName(typ);

        int found = thing.indexOf(':');
        if (thing.substring(found + 1) != "NotThing")
        {
            String a = StringTool.TakeString(thing.substring(found + 1), '{', '}')[0];

            if (typ == "byte")
            {
                a = a.replace(" ", "");
                return StringTool.HexToBytes(a);
            }
            else
            {
                String[] b = null;
                if (typ == "object")
                {
                    b = StringTool.TakeString(a, '{', '}');
                    for (int i = 0; i < b.length; i++)
                    {
                        int index = a.indexOf("{" + b[i] + "}");
                        a = a.substring(0, index) + "[" + i + "]" + a.substring(index + b[i].length() + 2);
                    }
                    String[] bb = StringTool.SplitWithFormat(a, ',');
                    for (int i = 0; i < bb.length; i++)
                    {
                        for (int ii = 0; ii < b.length; ii++)
                        {
                            bb[i] = bb[i].replace("[" + ii + "]", "{" + b[ii] + "}");
                        }
                    }
                    b = bb;
                }
                else
                {
                    b = StringTool.SplitWithFormat(a, ',');
                }

                Object output;
                
                if (typ == "object")
                {
                	output = new Object[b.length];
                    for (int i = 0; i < b.length; i++)
                    {
                    	((Object[])output)[i] = ChuonStringDeserializeToObject(b[i]);
                    }
                }
                else
                {
                	output = Array.newInstance(TypeFormat.TypeNameToType(typenames), b.length);
                    for (int i = 0; i < b.length; i++)
                    {
                        Object data;
                        switch (typ)
                        {
	                        case "ulong":
		                        {
		                        	Scanner sc = new Scanner(b[i]);
		                        	data = sc.nextBigInteger().longValue();
		                            sc.close();
		                        	break;
		                        }
	                        case "decimal":
		                        {
		                        	Scanner sc = new Scanner(b[i]);
		                        	data = new Decimal(sc.nextBigDecimal());
		                            sc.close();
		                        	break;
		                        }
                            case "char":
                                {
                                    data = StringTool.TakeString(b[i], '\'', '\'')[0].charAt(0);
                                    break;
                                }
                            case "string":
                                {
                                    data = StringTool.TakeString(b[i], '\"', '\"')[0];
                                    break;
                                }
                            case "bool":
                                {
                                    data = Boolean.parseBoolean(b[i].replace(" ", ""));
                                    break;
                                }
                            default:
	                            {
	                            	Scanner sc = new Scanner(b[i]);
	                            	Method next = Scanner.class.getMethod("next" + TypeFormat.ToJavaScannrTrueTypeName(typ));

	                            	data = next.invoke(sc);
	                                sc.close();
	                            	break;
	                            }
                        }
                        Array.set(output, i, data);
                    }
                }
                return output;
            }
        }
        else
        {
            return Array.newInstance(TypeFormat.TypeNameToType(typenames), 0);
        }
    }

    static Object StringToObjectForNotArray(String thing) throws Exception
    {
    	String[] vs = StringTool.SplitWithFormat(thing, ':');
    	String typ = StringTool.RemoveString(vs[0], " ", "\n", "\r", "\t", "[", "]");

    	String typenames = TypeFormat.ToJavaTrueTypeName(typ);

        int found = thing.indexOf(':');
        String a = thing.substring(found + 1);
        Object data;
        switch (typ)
        {
            case "ulong":
                {
                	Scanner sc = new Scanner(a);
                	data = sc.nextBigInteger().longValue();
                    sc.close();
                	break;
                }
            case "decimal":
                {
                	Scanner sc = new Scanner(a);
                	data = new Decimal(sc.nextBigDecimal());
                    sc.close();
                	break;
                }
            case "char":
                {
                    data = StringTool.TakeString(a, '\'', '\'')[0].charAt(0);
                    break;
                }
            case "string":
                {
                    data = StringTool.TakeString(a, '\"', '\"')[0];
                    break;
                }
            case "bool":
                {
                    data = Boolean.parseBoolean(a.replace(" ", ""));
                    break;
                }
            default:
                {
                	Scanner sc = new Scanner(a);
                	Method next = Scanner.class.getMethod("next" + TypeFormat.ToJavaScannrTrueTypeName(typ));

                	data = next.invoke(sc);
                    sc.close();
                	break;
                }
        }
        return data;
    }

    static Object ChuonStringDeserializeToObject(String thing) throws Exception
    {
        String[] vs = StringTool.SplitWithFormat(thing, ':');
        String typ = TypeFormat.ToSimpleTypeName(StringTool.RemoveString(vs[0], " ", "\n", "\r", "\t"));
        Object get;
        if (typ.contains("[]"))
        {
            get = StringToObjectForArray(thing);
        }
        else if (typ == "Dictionary")
        {
            int found = thing.indexOf(':');
            String _data = StringTool.TakeString(thing.substring(found + 1), '{', '}')[0];

            int data_index = _data.indexOf(':');
            int data_index2 = _data.indexOf(':', data_index + 1);

            String[] data = new String[] { _data.substring(0, data_index), _data.substring(data_index + 1, data_index2), _data.substring(data_index2 + 1) };
            data[0] = StringTool.RemoveString(data[0], new String[] { " ", "\n", "\r", "\t" });
            data[1] = StringTool.RemoveString(data[1], new String[] { " ", "\n", "\r", "\t" });
            String[] typenames = new String[] { TypeFormat.typelist2[Arrays.asList(TypeFormat.typelist).indexOf(data[0])], TypeFormat.typelist2[Arrays.asList(TypeFormat.typelist).indexOf(data[1])] };
            
            get = new HashMap();

            if (data[2] != "NotThing")
            {
                String[] a = StringTool.TakeString(data[2], '{', '}');

                for (int i = 0; i < a.length; i++)
                {
                    String[] b = StringTool.TakeString(a[i], '{', '}');
                    for (int ii = 0; ii < b.length; ii++)
                    {
                        int index = a[i].indexOf("{" + b[ii] + "}");
                        a[i] = a[i].substring(0, index) + "[" + ii + "]" + a[i].substring(index + b[ii].length() + 2);
                    }
                    String[] nowdata = StringTool.SplitWithFormat(a[i], ',');
                    for (int ii = 0; ii < nowdata.length; ii++)
                    {
                        for (int iii = 0; iii < b.length; iii++)
                        {
                            nowdata[ii] = nowdata[ii].replace("[" + iii + "]", "{" + b[iii] + "}");
                        }
                    }
                    Object key = ChuonStringDeserializeToObject(nowdata[0]);
                    Object value = ChuonStringDeserializeToObject(nowdata[1]);
                    ((Map)get).put(key, value);
                }
            }
        }
        else if (typ == "null")
        {
            get = null;
        }
        else if (Arrays.asList(TypeFormat.typelist).indexOf(typ) != -1)
        {
            get = StringToObjectForNotArray(thing);
        }
        else
        {
            get = typ;
        }
        return get;
    }

    static String BeforeFormatString(String input, char[] a)
    {
        StringBuilder stringBuilder = new StringBuilder(input);
        for (int i = 0; i < stringBuilder.length(); i++)
        {
            if (Arrays.asList(a).indexOf(stringBuilder.charAt(i)) >= 0 || stringBuilder.charAt(i) == '\\')
            {
                stringBuilder.insert(i, "\\");
                i++;
            }
        }
        return stringBuilder.toString();
    }

    static String printTab(boolean enable, int cont)
    {
        if (!enable)
        {
            return "";
        }
        String ans = "\r\n";
        for (int i = 0; i < cont; i++)
        {
            ans += "\t";
        }
        return ans;
    }
}
