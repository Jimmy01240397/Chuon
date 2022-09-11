package chuon;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;

public class ChuonString {
    String data = "";
    
    ChuonString()
    {
    	
    }

    public ChuonString(Object thing) throws Exception
    {
    	Class[] basetype = new Class[1];
        int rank = TypeFormat.ArrayRank(basetype, thing);
        TypeFormat.typing nowtypedata = TypeFormat.instance().get(basetype[0]);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(nowtypedata.getname());
        for(int i = 0; i < rank; i++)
        {
            stringBuilder.append("[]");
        }
        stringBuilder.append(":");
        stringBuilder.append(Typing(nowtypedata, thing, rank));
        data = stringBuilder.toString();
    }
    
    public ChuonString(String thing) throws Exception
    {
        data = thing;
        data = toChuonBinary().toChuonString().toString();
    }
    
    public ChuonString(byte[] thing, Charset encoding) throws Exception
    {
        data = new String(thing, encoding);
        data = toChuonBinary().toChuonString().toString();
    }

    public Object toObject() throws Exception
    {
        Object output = null;
        String[] splitdata = StringTool.SplitWithFormatWithoutinArray(data, ':');
        String type = StringTool.RemoveString(splitdata[0], " ", "\n", "\r", "\t");
        String basetype = StringTool.RemoveString(type, "[]");
        int rank = StringTool.TakeString(type, '[', ']').length;
        TypeFormat.typing nowtypedata = TypeFormat.instance().get(basetype);
        String alldata = splitdata[1];
        /*if (StringTool.RemoveString(alldata, " ", "\n", "\r", "\t").equals("null")) return null;
        if (rank > 0)
        {
            alldata = StringTool.TakeString(splitdata[1], '{', '}')[0];
        }*/
        output = GetTyp(nowtypedata, rank, alldata);
        return output;
    }

    public ChuonBinary toChuonBinary() throws Exception
    {
        String[] splitdata = StringTool.SplitWithFormatWithoutinArray(data, ':');
        String type = StringTool.RemoveString(splitdata[0], " ", "\n", "\r", "\t");
        String basetype = StringTool.RemoveString(type, "[]");
        int rank = StringTool.TakeString(type, '[', ']').length;
        TypeFormat.typing nowtypedata = TypeFormat.instance().get(basetype);
        String allstringdata = splitdata[1];
        /*if (rank > 0)
        {
            allstringdata = StringTool.TakeString(splitdata[1], '{', '}')[0];
        }*/
        byte[] alldata = ChuonBinaryTyping(nowtypedata, rank, allstringdata);
        if(alldata == null)
        {
            nowtypedata = TypeFormat.instance().get(Object.class);
            rank = 0;
            alldata = ChuonBinaryTyping(nowtypedata, rank, allstringdata);
        }
        byte[] ans = new byte[alldata.length + 2];
        ans[0] = nowtypedata.getindex();
        ans[1] = (byte)rank;
        System.arraycopy(alldata, 0, ans, 2, alldata.length);
        ChuonBinary output = new ChuonBinary();
        output.data = ans;
        return output;
    }
    
    public String toString()
    {
        return data;
    }

    public String toStringWithEnter()
    {
        StringBuilder ans = new StringBuilder();
        int last = 0;
        for(int i = 0, tab = 0; i < data.length(); i++)
        {
            switch(data.charAt(i))
            {
                case '\\':
                    {
                        i++;
                        break;
                    }
                case ',':
                    {
                        ans.append(data.substring(last, i + 1) + printTab(tab));
                        last = i + 1;
                        break;
                    }
                case '{':
                    {
                        if(data.charAt(i - 1) == '}' || data.charAt(i - 1) == ':') ans.append(data.substring(last, i) + printTab(tab));
                        last = i;
                        tab++;
                        ans.append(data.substring(last, i + 1) + printTab(tab));
                        last++;
                        break;
                    }
                case '}':
                    {
                        tab--;
                        ans.append(data.substring(last, i) + printTab(tab));
                        last = i;
                        ans.append(data.substring(last, i + 1));
                        last++;
                        break;
                    }
            }
        }
        ans.append(data.substring(last, data.length()));
        return ans.toString();
    }

    public byte[] toBinaryArray(Charset encoding)
    {
        return data.getBytes(encoding);
    }

    static String Typing(TypeFormat.typing nowtypedata, Object thing, int rank) throws Exception
    {
        if (thing == null) return "null";
        thing = TypeFormat.PrimitiveAndClassArray(thing, TypeFormat.ToBuildinType);
        StringBuilder ans = new StringBuilder();
        if (rank > 0) ans.append("{");
        if (rank >= nowtypedata.AllSerializationFunc.length)
        {
            if (rank > 0)
            {
                for (int i = 0; i < Array.getLength(thing); i++)
                {
                    String thingdata = Typing(nowtypedata, Array.get(thing, i), rank - 1);
                    ans.append(thingdata + ",");
                }
                if(ans.charAt(ans.length() - 1) == ',')
                    ans.delete(ans.length() - 1, ans.length());
            }
        }
        else
        {
            ans.append(nowtypedata.AllSerializationFunc[rank].DataToString(thing));
        }
        if (rank > 0) ans.append("}");
        return ans.toString();
    }

    static Object GetTyp(TypeFormat.typing nowtypedata, int rank, String nowdata) throws Exception
    {
        Class nowtype = nowtypedata.gettype();
        if (StringTool.RemoveString(nowdata, " ", "\n", "\r", "\t").equals("null")) return null;
        if (rank > 0)
        {
            nowdata = StringTool.TakeString(nowdata, '{', '}')[0];
        }
        Object ans = null;
        if (rank >= nowtypedata.AllSerializationFunc.length)
        {
            if (rank > 0)
            {
                for (int i = 0; i < rank - 1; i++)
                {
                    nowtype = Array.newInstance(nowtype, 0).getClass();
                }
                String[] alldata = StringTool.SplitWithFormatWithoutinArray(nowdata, ',');
                ans = Array.newInstance(nowtype, alldata.length);
                for (int i = 0; i < alldata.length; i++)
                {
                    /*if (StringTool.RemoveString(alldata[i], " ", "\n", "\r", "\t").equals("null"))
                        Array.set(ans, i, null);
                    else
                    {*/
                        //if (rank - 1 > 0) alldata[i] = StringTool.TakeString(alldata[i], '{', '}')[0];
                    Array.set(ans, i, GetTyp(nowtypedata, rank - 1, alldata[i]));
                    //}
                }
            }
        }
        else
        {
            ans = nowtypedata.AllSerializationFunc[rank].StringToData(nowdata);
        }
        return ans;
    }

    static byte[] ChuonBinaryTyping(TypeFormat.typing nowtypedata, int rank, String nowdata) throws Exception
    {
        if (StringTool.RemoveString(nowdata, " ", "\n", "\r", "\t").equals("null") && !(nowtypedata.gettype() == Object.class && rank == 0)) return null;
        if (rank > 0)
        {
            nowdata = StringTool.TakeString(nowdata, '{', '}')[0];
        }
        byte[] ans = null;
        if (rank >= nowtypedata.AllSerializationFunc.length)
        {
            if (rank > 0)
            {
                try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
                {
	                try (DataOutputStream writer = new DataOutputStream(stream))
	                {
	                    String[] alldata = StringTool.SplitWithFormatWithoutinArray(nowdata, ',');
	                    byte[] len = TypeFormat.GetBytesLength(alldata.length);
	
	                    writer.write(len, 0, len.length);
	                    
	                    try (ByteArrayOutputStream stream2 = new ByteArrayOutputStream())
	                    {
	    	                try (DataOutputStream writer2 = new DataOutputStream(stream2))
	                        {
	                            byte nullbool = 0;
	                            for (int i = 0; i < alldata.length; i++)
	                            {
	                                byte[] getdata = ChuonBinaryTyping(nowtypedata, rank - 1, alldata[i]);
	                                if ((rank > 1 || nowtypedata.getcannull()) && alldata.length > 0)
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
	                                    if (getdata == null)
	                                    {
	                                        nullbool++;
	                                    }
	                                }
	                                if (getdata != null)
	                                {
	                                    writer2.write(getdata);
	                                }
	                            }
	                            if ((rank > 1 || nowtypedata.getcannull()) && alldata.length > 0) writer.writeByte(nullbool);
	                            writer2.close();
	                            stream2.close();
	                            writer.write(stream2.toByteArray());
	                        }
	                    }

	                    
	                    /*if ((rank > 1 || nowtypedata.getcannull()) && alldata.length > 0)
	                    {
	                        byte nullbool = 0;
	                        for (int i = 0; i < alldata.length; i++)
	                        {
	                            if (i % 8 == 0)
	                            {
	                                if (i != 0)
	                                {
	                                    writer.write(nullbool);
	                                }
	                                nullbool = 0;
	                            }
	                            nullbool <<= 1;
	                            if (alldata[i].equals("null"))
	                            {
	                                nullbool++;
	                            }
	                        }
	                        writer.write(nullbool);
	                    }
	
	                    for (int i = 0; i < alldata.length; i++)
	                    {
	                        if (rank - 1 > 0) if (!alldata[i].equals("null")) alldata[i] = StringTool.TakeString(alldata[i], '{', '}')[0];
	                        if (!(rank > 1 || nowtypedata.getcannull()) || !alldata[i].equals("null"))
	                        {
	                            writer.write(ChuonBinaryTyping(nowtypedata, rank - 1, alldata[i]));
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
            ans = nowtypedata.AllSerializationFunc[rank].StringToBinary(nowdata);
        }
        return ans;
    }

    private static String printTab(int cont)
    {
    	String ans = "\r\n";
        for (int i = 0; i < cont; i++)
        {
            ans += "\t";
        }
        return ans;
    }
}
