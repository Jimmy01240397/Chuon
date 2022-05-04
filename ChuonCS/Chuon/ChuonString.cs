using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Chuon
{
    public class ChuonString
    {
        string data = "";
        public ChuonString(object thing)
        {
            int rank = TypeFormat.ArrayRank(out Type basetype, ref thing);
            TypeFormat.typing nowtypedata = TypeFormat.Instance[basetype];

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.Append(nowtypedata.name);
            for(int i = 0; i < rank; i++)
            {
                stringBuilder.Append("[]");
            }
            stringBuilder.Append(":");
            stringBuilder.Append(Typing(nowtypedata, thing, rank));
            data = stringBuilder.ToString();
        }

        public ChuonString(string thing)
        {
            data = thing;
            object test = ToObject();
            data = new ChuonString(test).ToString();
        }

        public ChuonString(byte[] thing, Encoding encoding)
        {
            data = encoding.GetString(thing);
            data = new ChuonString(ToObject()).ToString();
        }

        public object ToObject()
        {
            object output = null;
            string[] splitdata = StringTool.SplitWithFormatWithoutinArray(data, ':');
            string type = splitdata[0].RemoveString(" ", "\n", "\r", "\t");
            string basetype = type.RemoveString("[]");
            int rank = type.TakeString('[', ']').Length;
            TypeFormat.typing nowtypedata = TypeFormat.Instance[basetype];
            string alldata = splitdata[1];
            if (rank > 0)
            {
                alldata = splitdata[1].TakeString('{', '}')[0];
            }
            output = GetTyp(nowtypedata, rank, alldata);
            return output;
        }

        public ChuonBinary ToChuonBinary()
        {
            string[] splitdata = StringTool.SplitWithFormatWithoutinArray(data, ':');
            string type = splitdata[0].RemoveString(" ", "\n", "\r", "\t");
            string basetype = type.RemoveString("[]");
            int rank = type.TakeString('[', ']').Length;
            TypeFormat.typing nowtypedata = TypeFormat.Instance[basetype];
            string allstringdata = splitdata[1];
            if (rank > 0)
            {
                allstringdata = splitdata[1].TakeString('{', '}')[0];
            }
            byte[] alldata = ChuonBinaryTyping(nowtypedata, rank, allstringdata);
            byte[] ans = new byte[alldata.Length + 2];
            ans[0] = nowtypedata.index;
            ans[1] = (byte)rank;
            alldata.CopyTo(ans, 2);
            return new ChuonBinary(ans);
        }

        public override string ToString()
        {
            return data;
        }

        public string ToStringWithEnter()
        {
            StringBuilder ans = new StringBuilder();
            int last = 0;
            for(int i = 0, tab = 0; i < data.Length; i++)
            {
                switch(data[i])
                {
                    case '\\':
                        {
                            i++;
                            break;
                        }
                    case ',':
                        {
                            ans.Append(data.Substring(last, i - last + 1) + printTab(tab));
                            last = i + 1;
                            break;
                        }
                    case '{':
                        {
                            if(data[i - 1] == '}' || data[i - 1] == ':') ans.Append(data.Substring(last, i - last) + printTab(tab));
                            last = i;
                            tab++;
                            ans.Append(data.Substring(last, i - last + 1) + printTab(tab));
                            last++;
                            break;
                        }
                    case '}':
                        {
                            tab--;
                            ans.Append(data.Substring(last, i - last) + printTab(tab));
                            last = i;
                            ans.Append(data.Substring(last, i - last + 1));
                            last++;
                            break;
                        }
                }
            }
            ans.Append(data.Substring(last, data.Length - last));
            return ans.ToString();
        }

        public byte[] ToBinaryArray(Encoding encoding)
        {
            return encoding.GetBytes(data);
        }

        internal static string Typing(TypeFormat.typing nowtypedata, object thing, int rank)
        {
            if (thing == null) return "null";
            StringBuilder ans = new StringBuilder();
            if (rank > 0) ans.Append("{");
            if (rank >= nowtypedata.AllSerializationFunc.Length)
            {
                if (rank > 0)
                {
                    for (int i = 0; i < ((Array)thing).Length; i++)
                    {
                        string thingdata = Typing(nowtypedata, ((Array)thing).GetValue(i), rank - 1);
                        ans.Append(thingdata + ",");
                    }
                    if(ans[ans.Length - 1] == ',')
                        ans.Remove(ans.Length - 1, 1);
                }
            }
            else
            {
                ans.Append(nowtypedata.AllSerializationFunc[rank].DataToString(thing));
            }
            if (rank > 0) ans.Append("}");
            return ans.ToString();
        }

        internal static object GetTyp(TypeFormat.typing nowtypedata, int rank, string nowdata)
        {
            Type nowtype = nowtypedata.type;
            object ans = null;
            if (nowdata.RemoveString(" ", "\n", "\r", "\t") == "null") return null;
            if (rank >= nowtypedata.AllSerializationFunc.Length)
            {
                if (rank > 0)
                {
                    for (int i = 0; i < rank - 1; i++)
                    {
                        nowtype = nowtype.MakeArrayType();
                    }
                    string[] alldata = StringTool.SplitWithFormatWithoutinArray(nowdata, ',');
                    ans = Array.CreateInstance(nowtype, alldata.Length);
                    for (int i = 0; i < alldata.Length; i++)
                    {
                        if (alldata[i].RemoveString(" ", "\n", "\r", "\t") == "null")
                            ((Array)ans).SetValue(null, i);
                        else
                        {
                            if (rank - 1 > 0) alldata[i] = alldata[i].TakeString('{', '}')[0];
                            ((Array)ans).SetValue(GetTyp(nowtypedata, rank - 1, alldata[i]), i);
                        }
                    }
                }
            }
            else
            {
                ans = nowtypedata.AllSerializationFunc[rank].StringToData(nowdata);
            }
            return ans;
        }

        internal static byte[] ChuonBinaryTyping(TypeFormat.typing nowtypedata, int rank, string nowdata)
        {
            byte[] ans = null;
            if (rank >= nowtypedata.AllSerializationFunc.Length)
            {
                if (rank > 0)
                {
                    using (MemoryStream stream = new MemoryStream())
                    using (BinaryWriter writer = new BinaryWriter(stream))
                    {
                        string[] alldata = StringTool.SplitWithFormatWithoutinArray(nowdata, ',');
                        byte[] len = TypeFormat.GetBytesLength(alldata.Length);

                        writer.Write(len, 0, len.Length);
                        if ((rank > 1 || nowtypedata.cannull) && alldata.Length > 0)
                        {
                            byte nullbool = 0;
                            for (int i = 0; i < alldata.Length; i++)
                            {
                                if (i % 8 == 0)
                                {
                                    if (i != 0)
                                    {
                                        writer.Write(nullbool);
                                    }
                                    nullbool = 0;
                                }
                                nullbool <<= 1;
                                if (alldata[i] == "null")
                                {
                                    nullbool++;
                                }
                            }
                            writer.Write(nullbool);
                        }

                        for (int i = 0; i < alldata.Length; i++)
                        {
                            if (rank - 1 > 0) if (alldata[i] != "null") alldata[i] = alldata[i].TakeString('{', '}')[0];
                            if (!(rank > 1 || nowtypedata.cannull) || alldata[i] != "null")
                            {
                                writer.Write(ChuonBinaryTyping(nowtypedata, rank - 1, alldata[i]));
                            }
                        }
                        writer.Close();
                        stream.Close();
                        ans = stream.ToArray();
                    }
                }
            }
            else
            {
                ans = nowtypedata.AllSerializationFunc[rank].StringToBinary(nowdata);
            }
            return ans;
        }

        static string printTab(int cont)
        {
            string ans = "\r\n";
            for (int i = 0; i < cont; i++)
            {
                ans += "\t";
            }
            return ans;
        }

    }
}
