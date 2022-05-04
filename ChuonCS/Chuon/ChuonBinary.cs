using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Chuon
{
    public class ChuonBinary
    {

        byte[] data;
        public ChuonBinary(object thing)
        {
            int rank = TypeFormat.ArrayRank(out Type basetype, ref thing);
            TypeFormat.typing nowtypedata = TypeFormat.Instance[basetype];
            byte[] alldata = Typing(nowtypedata, thing, rank);
            data = new byte[alldata.Length + 2];
            data[0] = nowtypedata.index;
            data[1] = (byte)rank;
            alldata.CopyTo(data, 2);
        }

        public ChuonBinary(byte[] thing)
        {
            data = new byte[thing.Length];
            thing.CopyTo(data, 0);
            ToObject();
        }

        public ChuonBinary(byte[] thing, ref int index)
        {
            int nowindex = index;
            data = thing;
            ToObject(ref index);
            int len = index - nowindex;
            data = new byte[len];
            Array.Copy(thing, nowindex, data, 0, len);
        }

        public object ToObject(ref int index)
        {
            object output = null;
            if (data != null)
            {
                byte typeindex = data[index++], arrayrank = data[index++];
                output = GetTyp(TypeFormat.Instance[typeindex], data, arrayrank, ref index);
            }
            return output;
        }

        public object ToObject()
        {
            int index = 0;
            return ToObject(ref index);
        }

        public byte[] ToArray()
        {
            if (data != null)
            {
                byte[] output = new byte[data.Length];
                data.CopyTo(output, 0);
                return output;
            }
            return null;
        }

        public ChuonString ToChuonString()
        {
            int index = 0;
            return ToChuonString(ref index);
        }

        public ChuonString ToChuonString(ref int index)
        {
            string output = null;
            if (data != null)
            {
                byte typeindex = data[index++], arrayrank = data[index++];
                TypeFormat.typing nowtypedata = TypeFormat.Instance[typeindex];
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.Append(nowtypedata.name);
                for (int i = 0; i < arrayrank; i++)
                {
                    stringBuilder.Append("[]");
                }
                stringBuilder.Append(":");
                stringBuilder.Append(ChuonStringTyping(nowtypedata, data, arrayrank, ref index));
                output = stringBuilder.ToString();
            }
            return new ChuonString(output);
        }


        internal static byte[] Typing(TypeFormat.typing nowtypedata, object thing, int rank)
        {
            byte[] ans = null;
            if (rank >= nowtypedata.AllSerializationFunc.Length)
            {
                if (rank > 0)
                {
                    using (MemoryStream stream = new MemoryStream())
                    using (BinaryWriter writer = new BinaryWriter(stream))
                    {
                        byte[] len = TypeFormat.GetBytesLength(((Array)thing).Length);
                        writer.Write(len, 0, len.Length);
                        if ((rank > 1 || nowtypedata.cannull) && ((Array)thing).Length > 0)
                        {
                            byte nullbool = 0;
                            for (int i = 0; i < ((Array)thing).Length; i++)
                            {
                                if (i % 8 == 0)
                                {
                                    if(i != 0)
                                    {
                                        writer.Write(nullbool);
                                    }
                                    nullbool = 0;
                                }
                                nullbool <<= 1;
                                if (((Array)thing).GetValue(i) == null)
                                {
                                    nullbool++;
                                }
                            }
                            writer.Write(nullbool);
                        }
                        for (int i = 0; i < ((Array)thing).Length; i++)
                        {
                            if (!(rank > 1 || nowtypedata.cannull) || ((Array)thing).GetValue(i) != null)
                            {
                                byte[] thingdata = Typing(nowtypedata, ((Array)thing).GetValue(i), rank - 1);
                                writer.Write(thingdata, 0, thingdata.Length);
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
                ans = nowtypedata.AllSerializationFunc[rank].DataToBinary(thing);
            }
            return ans;
        }

        internal static object GetTyp(TypeFormat.typing nowtypedata, byte[] data, int rank, ref int index)
        {
            Type nowtype = nowtypedata.type;
            object ans = null;
            if (rank >= nowtypedata.AllSerializationFunc.Length)
            {
                if (rank > 0)
                {
                    int len = TypeFormat.GetIntLength(data, ref index);
                    for (int i = 0; i < rank - 1; i++)
                    {
                        nowtype = nowtype.MakeArrayType();
                    }
                    ans = Array.CreateInstance(nowtype, len);
                    bool[] allnullbools = new bool[len];
                    if (rank > 1 || nowtypedata.cannull)
                    {
                        byte nullbool = 0;
                        for (int i = 0; i < len; i++)
                        {
                            if (i % 8 == 0)
                            {
                                nullbool = data[index];
                                index++;
                            }
                            int nowleft = len - (i / 8 * 8) >= 8 ? 8 : len - (i / 8 * 8);
                            if ((i / 8 * 8) + (nowleft - 1 - (i % 8)) < allnullbools.Length)
                            {
                                allnullbools[(i / 8 * 8) + (nowleft - 1 - (i % 8))] = (nullbool & 1) == 1;
                            }
                            nullbool >>= 1;
                        }
                    }
                    for (int i = 0; i < len; i++)
                    {
                        if (allnullbools[i])
                        {
                            ((Array)ans).SetValue(null, i);
                        }
                        else
                        {
                            ((Array)ans).SetValue(GetTyp(nowtypedata, data, rank - 1, ref index), i);
                        }
                    }
                }
            }
            else
            {
                ans = nowtypedata.AllSerializationFunc[rank].BinaryToData(data, ref index);
            }
            return ans;
        }

        internal static string ChuonStringTyping(TypeFormat.typing nowtypedata, byte[] data, int rank, ref int index)
        {
            StringBuilder stringBuilder = new StringBuilder();
            if (rank > 0) stringBuilder.Append("{");
            if (rank >= nowtypedata.AllSerializationFunc.Length)
            {
                if (rank > 0)
                {
                    int len = TypeFormat.GetIntLength(data, ref index);
                    bool[] allnullbools = new bool[len];
                    if (rank > 1 || nowtypedata.cannull)
                    {
                        byte nullbool = 0;
                        for (int i = 0; i < len; i++)
                        {
                            if (i % 8 == 0)
                            {
                                nullbool = data[index];
                                index++;
                            }
                            int nowleft = len - (i / 8 * 8) >= 8 ? 8 : len - (i / 8 * 8);
                            if ((i / 8 * 8) + (nowleft - 1 - (i % 8)) < allnullbools.Length)
                            {
                                allnullbools[(i / 8 * 8) + (nowleft - 1 - (i % 8))] = (nullbool & 1) == 1;
                            }
                            nullbool >>= 1;
                        }
                    }
                    for (int i = 0; i < len; i++)
                    {
                        if (allnullbools[i])
                        {
                            stringBuilder.Append("null,");
                        }
                        else
                        {
                            stringBuilder.Append(ChuonStringTyping(nowtypedata, data, rank - 1, ref index) + ",");
                        }
                    }
                    if (stringBuilder[stringBuilder.Length - 1] == ',')
                        stringBuilder.Remove(stringBuilder.Length - 1, 1);
                }
            }
            else
            {
                stringBuilder.Append(nowtypedata.AllSerializationFunc[rank].BinaryToString(data, ref index));
            }
            if (rank > 0) stringBuilder.Append("}");
            return stringBuilder.ToString();
        }
    }
}
