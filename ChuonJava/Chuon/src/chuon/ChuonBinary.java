package chuon;

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
    
    ChuonBinary()
    {
    	
    }
    
    public ChuonBinary(Object thing) throws Exception
    {
    	Class[] basetype = new Class[1];
        int rank = TypeFormat.ArrayRank(basetype, thing);
        TypeFormat.typing nowtypedata = TypeFormat.instance().get(basetype[0]);
        byte[] alldata = Typing(nowtypedata, thing, rank);
        data = new byte[alldata.length + 2];
        data[0] = nowtypedata.getindex();
        data[1] = (byte)rank;
        System.arraycopy(alldata, 0, data, 2, alldata.length);
    }

    public ChuonBinary(byte[] thing) throws Exception
    {
        data = Arrays.copyOf(thing, thing.length);
        toObject();
    }

    public ChuonBinary(byte[] thing, int[] index) throws Exception
    {
        int nowindex = index[0];
        data = thing;
        toObject(index);
        int len = index[0] - nowindex;
        data = new byte[len];
        System.arraycopy(thing, nowindex, data, 0, len);
    }
    
    public Object toObject(int[] index) throws Exception
    {
        Object output = null;
        if (data != null)
        {
            byte typeindex = data[index[0]++], arrayrank = data[index[0]++];
            output = GetTyp(TypeFormat.instance().get(typeindex), data, arrayrank, index);
        }
        return output;
    }
    
    
    public Object toObject() throws Exception
    {
        int[] index = new int[] {0};
        return toObject(index);
    }

    public byte[] toArray()
    {
        if (data != null)
        {
            return Arrays.copyOf(data, data.length);
        }
        return null;
    }

    public ChuonString toChuonString(int[] index) throws Exception
    {
        String output = null;
        if (data != null)
        {
            byte typeindex = data[index[0]++], arrayrank = data[index[0]++];
            TypeFormat.typing nowtypedata = TypeFormat.instance().get(typeindex);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(nowtypedata.getname());
            for (int i = 0; i < arrayrank; i++)
            {
                stringBuilder.append("[]");
            }
            stringBuilder.append(":");
            stringBuilder.append(ChuonStringTyping(nowtypedata, data, arrayrank, index));
            output = stringBuilder.toString();
        }
        ChuonString ans = new ChuonString();
        ans.data = output;
        return ans;
    }
    
    public ChuonString toChuonString() throws Exception
    {
        int[] index = new int[] {0};
        return toChuonString(index);
    }
	
    static byte[] Typing(TypeFormat.typing nowtypedata, Object thing, int rank) throws Exception
    {
        if (thing == null && !(nowtypedata.gettype() == Object.class && rank == 0)) return null;
        byte[] ans = null;
        thing = TypeFormat.PrimitiveAndClassArray(thing, TypeFormat.ToBuildinType);
        if (rank >= nowtypedata.AllSerializationFunc.length)
        {
            if (rank > 0)
            {
                try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
                {
	                try (DataOutputStream writer = new DataOutputStream(stream))
	                {
	                    byte[] len = TypeFormat.GetBytesLength(Array.getLength(thing));
	                    writer.write(len, 0, len.length);
	                    
	                    try (ByteArrayOutputStream stream2 = new ByteArrayOutputStream())
	                    {
	    	                try (DataOutputStream writer2 = new DataOutputStream(stream2))
	    	                {
	                            byte nullbool = 0;
	                            for (int i = 0; i < Array.getLength(thing); i++)
	                            {
	                                byte[] thingdata = Typing(nowtypedata, Array.get(thing, i), rank - 1);
	                                if ((rank > 1 || nowtypedata.getcannull()) && Array.getLength(thing) > 0)
	                                {
	                                    if (i % 8 == 0)
	                                    {
	                                        if (i != 0)
	                                        {
	                                            writer.writeByte(nullbool);
	                                        }
	                                        nullbool = 0;
	                                    }
	                                    nullbool <<= 1;
	                                    if (thingdata == null)
	                                    {
	                                        nullbool++;
	                                    }
	                                }
	                                if(thingdata != null)
	                                {
	                                    writer2.write(thingdata, 0, thingdata.length);
	                                }
	                            }
	                            if ((rank > 1 || nowtypedata.getcannull()) && Array.getLength(thing) > 0) writer.writeByte(nullbool);
	                            writer2.close();
	                            stream2.close();
	                            writer.write(stream2.toByteArray());
	                        }
	                    }
	                    
	                    /*if ((rank > 1 || nowtypedata.getcannull()) && Array.getLength(thing) > 0)
	                    {
	                        byte nullbool = 0;
	                        for (int i = 0; i < Array.getLength(thing); i++)
	                        {
	                            if (i % 8 == 0)
	                            {
	                                if(i != 0)
	                                {
	                                    writer.write(nullbool);
	                                }
	                                nullbool = 0;
	                            }
	                            nullbool <<= 1;
	                            if (Array.get(thing, i) == null)
	                            {
	                                nullbool++;
	                            }
	                        }
	                        writer.write(nullbool);
	                    }
	                    for (int i = 0; i < Array.getLength(thing); i++)
	                    {
	                        if (!(rank > 1 || nowtypedata.getcannull()) || Array.get(thing, i) != null)
	                        {
	                            byte[] thingdata = Typing(nowtypedata, Array.get(thing, i), rank - 1);
	                            writer.write(thingdata, 0, thingdata.length);
	                        }
	                    }*/
	                    writer.close();
	                    stream.close();
	                    ans = stream.toByteArray();
	                }
                }
            }
        }
        else
        {
            ans = nowtypedata.AllSerializationFunc[rank].DataToBinary(thing);
        }
        return ans;
    }

    static Object GetTyp(TypeFormat.typing nowtypedata, byte[] data, int rank, int[] index) throws Exception
    {
        Class nowtype = nowtypedata.gettype();
        Object ans = null;
        if (rank >= nowtypedata.AllSerializationFunc.length)
        {
            if (rank > 0)
            {
                int len = TypeFormat.GetIntLength(data, index);
                for (int i = 0; i < rank - 1; i++)
                {
                    nowtype = Array.newInstance(nowtype, 0).getClass();
                }
                ans = Array.newInstance(nowtype, len);
                boolean[] allnullbools = new boolean[len];
                if (rank > 1 || nowtypedata.getcannull())
                {
                    byte nullbool = 0;
                    for (int i = 0; i < len; i++)
                    {
                        if (i % 8 == 0)
                        {
                            nullbool = data[index[0]];
                            index[0]++;
                        }
                        int nowleft = len - (i / 8 * 8) >= 8 ? 8 : len - (i / 8 * 8);
                        if ((i / 8 * 8) + (nowleft - 1 - (i % 8)) < allnullbools.length)
                        {
                            allnullbools[(i / 8 * 8) + (nowleft - 1 - (i % 8))] = (nullbool & 1) == 1;
                        }
                        nullbool >>>= 1;
                    }
                }
                for (int i = 0; i < len; i++)
                {
                    if (allnullbools[i])
                    {
                        Array.set(ans, i, null);
                    }
                    else
                    {
                    	Array.set(ans, i, GetTyp(nowtypedata, data, rank - 1, index));
                    }
                }
            }
        }
        else
        {
            ans = nowtypedata.AllSerializationFunc[rank].BinaryToData(data, index);
        }
        return ans;
    }

    static String ChuonStringTyping(TypeFormat.typing nowtypedata, byte[] data, int rank, int[] index) throws Exception
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (rank > 0) stringBuilder.append("{");
        if (rank >= nowtypedata.AllSerializationFunc.length)
        {
            if (rank > 0)
            {
                int len = TypeFormat.GetIntLength(data, index);
                boolean[] allnullbools = new boolean[len];
                if (rank > 1 || nowtypedata.getcannull())
                {
                    byte nullbool = 0;
                    for (int i = 0; i < len; i++)
                    {
                        if (i % 8 == 0)
                        {
                            nullbool = data[index[0]];
                            index[0]++;
                        }
                        int nowleft = len - (i / 8 * 8) >= 8 ? 8 : len - (i / 8 * 8);
                        if ((i / 8 * 8) + (nowleft - 1 - (i % 8)) < allnullbools.length)
                        {
                            allnullbools[(i / 8 * 8) + (nowleft - 1 - (i % 8))] = (nullbool & 1) == 1;
                        }
                        nullbool >>>= 1;
                    }
                }
                for (int i = 0; i < len; i++)
                {
                    if (allnullbools[i])
                    {
                        stringBuilder.append("null,");
                    }
                    else
                    {
                        stringBuilder.append(ChuonStringTyping(nowtypedata, data, rank - 1, index) + ",");
                    }
                }
                if (stringBuilder.charAt(stringBuilder.length() - 1) == ',')
                    stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            }
        }
        else
        {
            stringBuilder.append(nowtypedata.AllSerializationFunc[rank].BinaryToString(data, index));
        }
        if (rank > 0) stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
