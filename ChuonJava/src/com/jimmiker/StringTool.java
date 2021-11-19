package com.jimmiker;

import java.util.ArrayList;
import java.util.Arrays;

public class StringTool {
    final static char[] format = { '\t', '\n', '\r', '\f', '\'', '\"', '\\', '{', '}', '[', ']', ',', ':' };
    final static char[] unformat = { 't', 'n', 'r', 'f' };
	
    static int Matches(String input, char a)
    {
    	String[] j = SplitWithFormat(input, a);
        return j.length + 1;
    }

    public static String Escape(String input)
    {
        for (int i = 0; i < input.length(); i++)
        {
            if (IsMetachar(input.charAt(i)))
            {
                StringBuilder stringBuilder = new StringBuilder();
                char c = input.charAt(i);
                stringBuilder.append(input, 0, i);
                do
                {
                    stringBuilder.append('\\');
                    switch (c)
                    {
                        case '\t':
                            c = 't';
                            break;
                        case '\n':
                            c = 'n';
                            break;
                        case '\f':
                            c = 'f';
                            break;
                        case '\r':
                            c = 'r';
                            break;
                    }
                    stringBuilder.append(c);
                    i++;
                    int num = i;
                    while (i < input.length())
                    {
                        c = input.charAt(i);
                        if (IsMetachar(c))
                        {
                            break;
                        }
                        i++;
                    }
                    stringBuilder.append(input, num, i);
                }
                while (i < input.length());
                return stringBuilder.toString();
            }
        }
        return input;
    }

    public static String Unescape(String input)
    {
        StringBuilder stringBuilder = new StringBuilder(input);
        for (int i = 0; i < stringBuilder.length(); i++)
        {
            if (stringBuilder.charAt(i) == '\\')
            {
                stringBuilder.delete(i, i + 1);
                if (Arrays.asList(unformat).contains(stringBuilder.charAt(i)))
                {
                    stringBuilder.setCharAt(i, format[Arrays.asList(unformat).indexOf(stringBuilder.charAt(i))]);;
                }
            }
        }
        return stringBuilder.toString();
    }

    static boolean IsMetachar(char ch)
    {
        return Arrays.asList(format).contains(ch);
    }

    public static String[] SplitWithFormat(String input, char a)
    {
        ArrayList<String> vs = new ArrayList<String>();
        int now = 0;
        for (int i = 0; i < input.length(); i++)
        {
            if (input.charAt(i) == '\\')
            {
                i++;
            }
            else if (input.charAt(i) == a)
            {
                vs.add(input.substring(now, i));
                now = i + 1;
            }
        }
        vs.add(input.substring(now, input.length()));
        String[] outdata = new String[vs.size()];
        return vs.toArray(outdata);
    }

    public static String[] TakeString(String text, Character a, Character b)
    {
        ArrayList<String> q = new ArrayList<String>(Arrays.asList(SplitWithFormat(text, b)));
        if (a == b)
        {
            if (q.size() % 2 == 0)
            {
                q.remove(q.size() - 1);
            }
            for (int i = 0; i < q.size(); i++)
            {
                q.remove(i);
            }
            String[] outdata = new String[q.size()];
            return q.toArray(outdata);
        }
        q.remove(q.size() - 1);
        for (int i = 0; i < q.size();)
        {
            if (!q.get(i).equals(""))
            {
                if (Matches(q.get(i), a) != Matches(q.get(i), b) + 1)
                {
                	q.set(i, q.get(i) + b.toString() + q.get(i + 1));
                    q.remove(i + 1);
                }
                else
                {
                    i++;
                }
            }
            else
            {
            	q.set(i - 1, q.get(i - 1) + b.toString());
                q.remove(i);
            }
        }
        ArrayList<String> vs = new ArrayList<String>();
        for (int ii = 0; ii < q.size(); ii++)
        {
        	String s = q.get(ii);
            int found = 0;
            for (int i = 0; i < s.length(); i++)
            {
                if (s.charAt(i) == '\\')
                {
                    i++;
                }
                else if (s.charAt(i) == a)
                {
                    found = i;
                    break;
                }
            }
            if (found != -1)
            {
                if (found + 1 == s.length())
                {
                    vs.add("");
                }
                else
                {
                    vs.add(s.substring(found + 1));
                }
            }
        }
        String[] outdata = new String[vs.size()];
        return vs.toArray(outdata);
    }

    public static String RemoveString(String input, String... arg)
    {
        for(int i = 0; i < arg.length; i++)
        {
            input = input.replaceAll(arg[i], "");
        }
        return input;
    }
	    
	static public byte[] HexToBytes(String str) throws Exception
	{
	    str = StringTool.RemoveString(str, new String[] { " ", "\n", "\r", "\t" });
	    byte[] bytes = new byte[str.length() / 2];
	    int j = 0;
	    
	    Func<String, Byte> HexToByte = new Func<String, Byte>() {
			
			@Override
			public Byte run(String... hex) {
		        if (hex[0].length() > 2 || hex[0].length() <= 0)
		            throw new IllegalArgumentException("hex must be 1 or 2 characters in length");
		        byte newByte = (byte) Integer.parseInt(hex[0],16);
		        return newByte;
			}
		};
	    
	    for (int i = 0; i < bytes.length; i++)
	    {
	        String hex = new String(new char[] { str.charAt(j), str.charAt(j + 1) });
	        bytes[i] = HexToByte.run(hex);
	        j = j + 2;
	    }
	    return bytes;
	}
	
	static public String BytesToHex(byte[] bytes)
	{
	    StringBuilder str2 = new StringBuilder();
	    for (int i = 0; i < bytes.length; i++)
	    {
	        str2.append(String.format("%02X", bytes[i]));
	    }
	    return str2.toString();
	}
}
