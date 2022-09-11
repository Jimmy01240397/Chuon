using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace Chuon
{
    class TypeFormat
    {
        public struct SerializationFunc
        {
            public delegate T BinaryToT<T>(byte[] data, ref int index);
            public Func<object, byte[]> DataToBinary { get; private set; }
            public BinaryToT<object> BinaryToData { get; private set; }
            public Func<object, string> DataToString { get; private set; }
            public Func<string, object> StringToData { get; private set; }
            public BinaryToT<string> BinaryToString { get; private set; }
            public Func<string, byte[]> StringToBinary { get; private set; }

            void setup(Func<object, byte[]> DataToBinary, Func<object, string> DataToString, BinaryToT<string> BinaryToString, BinaryToT<object> BinaryToData, Func<string, object> StringToData, Func<string, byte[]> StringToBinary)
            {
                if (DataToBinary == null && DataToString != null && StringToBinary != null)
                {
                    DataToBinary = (data) =>
                    {
                        return StringToBinary(DataToString(data));
                    };
                }
                else if (DataToBinary == null) throw new ArgumentNullException("DataToBinary");
                if (DataToString == null && DataToBinary != null && BinaryToString != null)
                {
                    DataToString = (data) =>
                    {
                        int index = 0;
                        return BinaryToString(DataToBinary(data), ref index);
                    };
                }
                else if (DataToString == null) throw new ArgumentNullException("DataToString");
                if (BinaryToString == null && BinaryToData != null && DataToString != null)
                {
                    BinaryToString = (byte[] data, ref int index) =>
                    {
                        return DataToString(BinaryToData(data, ref index));
                    };
                }
                else if (BinaryToString == null) throw new ArgumentNullException("BinaryToString");
                if (BinaryToData == null && BinaryToString != null && StringToData != null)
                {
                    BinaryToData = (byte[] data, ref int index) =>
                    {
                        return StringToData(BinaryToString(data, ref index));
                    };
                }
                else if (BinaryToData == null) throw new ArgumentNullException("BinaryToData");
                if (StringToData == null && StringToBinary != null && BinaryToData != null)
                {
                    StringToData = (data) =>
                    {
                        int index = 0;
                        return BinaryToData(StringToBinary(data), ref index);
                    };
                }
                else if (StringToData == null) throw new ArgumentNullException("StringToData");
                if (StringToBinary == null && StringToData != null && DataToBinary != null)
                {
                    StringToBinary = (data) =>
                    {
                        return DataToBinary(StringToData(data));
                    };
                }
                else if (StringToBinary == null) throw new ArgumentNullException("StringToBinary");
                this.DataToBinary = DataToBinary;
                this.DataToString = DataToString;
                this.BinaryToString = BinaryToString;
                this.BinaryToData = BinaryToData;
                this.StringToData = StringToData;
                this.StringToBinary = StringToBinary;
            }
            public SerializationFunc(Func<object, byte[]> DataToBinary, Func<object, string> DataToString, BinaryToT<string> BinaryToString, BinaryToT<object> BinaryToData, Func<string, object> StringToData, Func<string, byte[]> StringToBinary)
            {
                this.DataToBinary = null;
                this.DataToString = null;
                this.BinaryToString = null;
                this.BinaryToData = null;
                this.StringToData = null;
                this.StringToBinary = null;
                setup(DataToBinary, DataToString, BinaryToString, BinaryToData, StringToData, StringToBinary);
            }

            public SerializationFunc(Func<object, byte[]> DataToBinary, Func<object, string> DataToString, BinaryToT<object> BinaryToData, Func<string, object> StringToData)
            {
                this.DataToBinary = null;
                this.DataToString = null;
                this.BinaryToString = null;
                this.BinaryToData = null;
                this.StringToData = null;
                this.StringToBinary = null;
                setup(DataToBinary, DataToString, null, BinaryToData, StringToData, null);
            }
            public SerializationFunc(Func<object, byte[]> DataToBinary, BinaryToT<string> BinaryToString, BinaryToT<object> BinaryToData, Func<string, byte[]> StringToBinary)
            {
                this.DataToBinary = null;
                this.DataToString = null;
                this.BinaryToString = null;
                this.BinaryToData = null;
                this.StringToData = null;
                this.StringToBinary = null;
                setup(DataToBinary, null, BinaryToString, BinaryToData, null, StringToBinary);
            }
            public SerializationFunc(Func<object, string> DataToString, BinaryToT<string> BinaryToString, Func<string, object> StringToData, Func<string, byte[]> StringToBinary)
            {
                this.DataToBinary = null;
                this.DataToString = null;
                this.BinaryToString = null;
                this.BinaryToData = null;
                this.StringToData = null;
                this.StringToBinary = null;
                setup(null, DataToString, BinaryToString, null, StringToData, StringToBinary);
            }
        }

        public struct typing
        {
            public byte index { get; private set; }
            public Type type { get; private set; }
            private string[] _names;
            public bool cannull { get; private set; }

            public string name
            {
                get
                {
                    return _names[0];
                }
            }

            public string[] names
            {
                get
                {
                    string[] tmp = new string[_names.Length];
                    _names.CopyTo(tmp, 0);
                    return tmp;
                }
            }

            public SerializationFunc[] AllSerializationFunc;

            public typing(byte index, Type type, string name, bool cannull, params SerializationFunc[] serializationFunc)
            {
                this.index = index;
                this.type = type;
                this._names = new string[] { name };
                this.cannull = cannull;
                if (serializationFunc.Length == 0) throw new ArgumentNullException("serializationFunc");
                this.AllSerializationFunc = serializationFunc;
            }

            public typing(byte index, Type type, string[] names, bool cannull, params SerializationFunc[] serializationFunc)
            {
                this.index = index;
                this.type = type;
                this._names = new string[names.Length];
                names.CopyTo(_names, 0);
                this.cannull = cannull;
                if (serializationFunc.Length == 0) throw new ArgumentNullException("serializationFunc");
                this.AllSerializationFunc = serializationFunc;
            }
        }

        List<typing> typings = new List<typing>();

        static TypeFormat _instance;

        public static TypeFormat Instance
        {
            get
            {
                if (_instance == null) _instance = new TypeFormat();
                return _instance;
            }
        }

        public typing this[int index]
        {
            get
            {
                for (int i = 0; i < typings.Count; i++)
                {
                    if (typings[i].index == index) return typings[i];
                }
                throw new IndexOutOfRangeException();
            }
        }

        public typing this[Type index]
        {
            get
            {
                for (int i = 0; i < typings.Count; i++)
                {
                    if (typings[i].type == index) return typings[i];
                }
                Type[] allinterfaces = index.GetInterfaces();
                foreach (Type nowtype in allinterfaces)
                {
                    for (int i = 0; i < typings.Count; i++)
                    {
                        if (typings[i].type == nowtype) return typings[i];
                    }
                }
                return this[index.BaseType];
                //throw new IndexOutOfRangeException();
            }
        }

        public typing this[string index]
        {
            get
            {
                for (int i = 0; i < typings.Count; i++)
                {
                    if (Array.IndexOf(typings[i].names, index) != -1) return typings[i];
                }
                throw new IndexOutOfRangeException();
            }
        }

        #region Length
        public static byte[] GetBytesLength(int cont)
        {
            List<byte> vs = new List<byte>();
            for (int i = cont / 128; i != 0; i = cont / 128)
            {
                vs.Add((byte)(cont % 128 + 128));
                cont = i;
            }
            vs.Add((byte)(cont % 128));
            return vs.ToArray();
        }

        public static int GetIntLength(byte[] data, ref int index)
        {
            List<byte> vs = new List<byte>();
            byte a;
            do
            {
                a = data[index];
                vs.Add((byte)(a % 128));
                index++;
            } while (a >= 128);
            int x = 0;
            for (int i = 0; i < vs.Count; i++)
            {
                x += (int)(vs[i] * Math.Pow(128, i));
            }
            return x;
        }
        #endregion

        internal static Array RebuildArray(Array array, Type basetype, int nowrank)
        {
            Type nowtype = basetype;
            for (int i = 0; i < nowrank - 1; i++)
            {
                nowtype = nowtype.MakeArrayType();
            }
            Array ans;
            if (array.Rank == 1)
            {
                if (array.GetType().GetElementType().IsArray)
                {
                    ans = Array.CreateInstance(nowtype, array.Length);
                    for (int i = 0; i < array.Length; i++)
                    {
                        ans.SetValue(RebuildArray((Array)array.GetValue(i), basetype, nowrank - 1), i);
                    }
                }
                else ans = array;
            }
            else
            {
                int[] ranklen = new int[array.Rank];
                for (int i = 0; i < ranklen.Length; i++)
                {
                    ranklen[i] = array.GetLength(ranklen.Length - i - 1);
                }
                Array makearray(Type arraybasetype, int arrayrank, List<int> index)
                {
                    Type arraynowtype = arraybasetype;
                    for (int i = 0; i < arrayrank - 1; i++)
                        arraynowtype = arraynowtype.MakeArrayType();
                    Array nowans = Array.CreateInstance(arraynowtype, ranklen[arrayrank - 1]);
                    for (int i = 0; i < ranklen[arrayrank - 1]; i++)
                    {
                        index.Add(i);
                        if (arrayrank > 1)
                            nowans.SetValue(makearray(arraybasetype, arrayrank - 1, index), i);
                        else
                            if (array.GetType().GetElementType().IsArray)
                            nowans.SetValue(RebuildArray((Array)array.GetValue(index.ToArray()), basetype, nowrank - index.Count), i);
                        else
                            nowans.SetValue(array.GetValue(index.ToArray()), i);
                        index.RemoveAt(index.Count - 1);
                    }
                    return nowans;
                }
                ans = makearray(array.GetType().GetElementType(), array.Rank, new List<int>());
            }
            return ans;
        }

        internal static int ArrayRank(out Type basetype, ref object thing)
        {
            if (thing == null)
            {
                basetype = typeof(object);
                return 0;
            }
            int rank = 0;
            bool allallow = true;
            for (basetype = thing.GetType(); basetype.GetElementType() != null; basetype = basetype.GetElementType())
            {
                if (basetype.IsArray)
                {
                    rank += basetype.GetArrayRank();
                    allallow = basetype.GetArrayRank() == 1 && allallow;
                }
            }

            if (!allallow)
            {
                thing = RebuildArray((Array)thing, basetype, rank);
            }
            return rank;
        }

        internal static int ArrayRank(out Type basetype, ref Type nowtype)
        {
            int rank = 0;
            bool allallow = true;
            for (basetype = nowtype; basetype.GetElementType() != null; basetype = basetype.GetElementType())
            {
                if (basetype.IsArray)
                {
                    rank += basetype.GetArrayRank();
                    allallow = basetype.GetArrayRank() == 1 && allallow;
                }
            }

            nowtype = basetype;
            for (int i = 0; i < rank; i++)
            {
                nowtype = nowtype.MakeArrayType();
            }
            return rank;
        }


        private TypeFormat()
        {
            #region object
            typings.Add(new typing(0, typeof(object), "object", false,
                new SerializationFunc(
                (data) =>
                {
                    if (data == null)
                        return new byte[2] { 0, 0 };
                    ChuonBinary chuonBinary = new ChuonBinary(data);
                    return chuonBinary.ToArray();
                }, 
                (data) =>
                {
                    if (data == null) return "null";
                    ChuonString chuonString = new ChuonString(data);
                    return chuonString.ToString();
                },
                (byte[] data, ref int index) =>
                {
                    if (data[index] == 0 && data[index + 1] == 0)
                    {
                        index += 2;
                        return null;
                    }
                    ChuonBinary chuonBinary = new ChuonBinary(data, ref index);
                    return chuonBinary.ToObject();
                },
                (data) =>
                {
                    if (data.RemoveString(" ", "\n", "\r", "\t") ==  "null") return null;
                    ChuonString chuonString = new ChuonString(data);
                    return chuonString.ToObject();
                }
            )));
            #endregion
            #region byte
            typings.Add(new typing(1, typeof(byte), "byte", false,
                new SerializationFunc(
                (data) => new byte[1] { (byte)data }, 
                Convert.ToString, 
                (byte[] data, ref int index) => data[index++], 
                (data) =>
                {
                    if (data.Length > 1 && data[0] == '0')
                    {
                        if (data[1] == 'b' || data[1] == 'B') return Convert.ToByte(data, 2);
                        else if (data[1] == 'x' || data[1] == 'X') return Convert.ToByte(data, 16);
                        else return Convert.ToByte(data, 8);
                    }
                    else
                        return Convert.ToByte(data);
                }), 
                new SerializationFunc(
                (data) =>
                {
                    byte[] len = GetBytesLength(((byte[])data).Length);
                    byte[] ans = new byte[len.Length + ((byte[])data).Length];
                    len.CopyTo(ans, 0);
                    ((byte[])data).CopyTo(ans, len.Length);
                    return ans;
                },
                (data) => StringTool.BytesToHex((byte[])data),
                (byte[] data, ref int index) =>
                {
                    int len = GetIntLength(data, ref index);
                    byte[] nowdata = new byte[len];
                    Array.Copy(data, index, nowdata, 0, len);
                    index += len;
                    return StringTool.BytesToHex(nowdata);
                },
                (byte[] data, ref int index) =>
                {
                    int len = GetIntLength(data, ref index);
                    byte[] nowdata = new byte[len];
                    Array.Copy(data, index, nowdata, 0, len);
                    index += len;
                    return nowdata;
                },
                (data) => StringTool.HexToBytes(data.RemoveString(" ")),
                (data) =>
                {
                    byte[] newdata = StringTool.HexToBytes(data.RemoveString(" "));
                    byte[] len = GetBytesLength(newdata.Length);
                    byte[] ans = new byte[len.Length + newdata.Length];
                    len.CopyTo(ans, 0);
                    newdata.CopyTo(ans, len.Length);
                    return ans;
                }
            )));
            #endregion

            byte[] HToNAndNToH(byte[] host)
            {
                byte[] bytes = new byte[host.Length];
                host.CopyTo(bytes, 0);

                if (BitConverter.IsLittleEndian)
                    Array.Reverse(bytes);

                return bytes;
            }

            SerializationFunc makeNumSerializationFunc<T> (Func<byte[], int, T> ConvertByte, Func<string, int, T> ConvertString, Func<T, byte[]> GetBytes, Func<object, T> objectToT) where T : IComparable
            {
                return new SerializationFunc(
                (data) => HToNAndNToH(GetBytes(objectToT(data))),
                Convert.ToString,
                (byte[] data, ref int index) =>
                {
                    byte[] nowdata = new byte[System.Runtime.InteropServices.Marshal.SizeOf(typeof(T))];
                    Array.Copy(data, index, nowdata, 0, nowdata.Length);
                    index += nowdata.Length;
                    return ConvertByte(HToNAndNToH(nowdata), 0);
                },
                (data) =>
                {
                    data = data.RemoveString(" ", "\n", "\r", "\t");
                    if (data.Length > 1 && data[0] == '0')
                    {
                        if (data[1] == 'b' || data[1] == 'B') return ConvertString(data, 2);
                        else if (data[1] == 'x' || data[1] == 'X') return ConvertString(data, 16);
                        else return ConvertString(data, 8);
                    }
                    else
                        return ConvertString(data, 10);
                });
            }

            #region short
            typings.Add(new typing(2, typeof(short), "short", false, makeNumSerializationFunc(BitConverter.ToInt16, Convert.ToInt16,  BitConverter.GetBytes, (data) => (short)data)));
            #endregion
            #region int
            typings.Add(new typing(3, typeof(int), "int", false, makeNumSerializationFunc(BitConverter.ToInt32, Convert.ToInt32, BitConverter.GetBytes, (data) => (int)data)));
            #endregion
            #region long
            typings.Add(new typing(4, typeof(long), "long", false, makeNumSerializationFunc(BitConverter.ToInt64, Convert.ToInt64, BitConverter.GetBytes, (data) => (long)data)));
            #endregion
            #region ushort
            typings.Add(new typing(5, typeof(ushort), "ushort", false, makeNumSerializationFunc(BitConverter.ToUInt16, Convert.ToUInt16, BitConverter.GetBytes, (data) => (ushort)data)));
            #endregion
            #region uint
            typings.Add(new typing(6, typeof(uint), "uint", false, makeNumSerializationFunc(BitConverter.ToUInt32, Convert.ToUInt32, BitConverter.GetBytes, (data) => (uint)data)));
            #endregion
            #region ulong
            typings.Add(new typing(7, typeof(ulong), "ulong", false, makeNumSerializationFunc(BitConverter.ToUInt64, Convert.ToUInt64,BitConverter.GetBytes, (data) => (ulong)data)));
            #endregion
            #region float
            typings.Add(new typing(8, typeof(float), "float", false, new SerializationFunc(
                (data) => HToNAndNToH(BitConverter.GetBytes((float)data)),
                Convert.ToString,
                (byte[] data, ref int index) =>
                {
                    byte[] nowdata = new byte[sizeof(float)];
                    Array.Copy(data, index, nowdata, 0, nowdata.Length);
                    index += nowdata.Length;
                    return BitConverter.ToSingle(HToNAndNToH(nowdata), 0);
                },
                (data) => Convert.ToSingle(data))));
            #endregion
            #region double
            typings.Add(new typing(9, typeof(double), "double", false, new SerializationFunc(
                (data) => HToNAndNToH(BitConverter.GetBytes((double)data)),
                Convert.ToString,
                (byte[] data, ref int index) =>
                {
                    byte[] nowdata = new byte[sizeof(double)];
                    Array.Copy(data, index, nowdata, 0, nowdata.Length);
                    index += nowdata.Length;
                    return BitConverter.ToDouble(HToNAndNToH(nowdata), 0);
                },
                (data) => Convert.ToDouble(data))));
            #endregion
            #region bool
            typings.Add(new typing(10, typeof(bool), "bool", false, 
                new SerializationFunc(
                (data) => ((bool)data ? new byte[] { 1 } : new byte[] { 0 }),
                (data) => ((bool)data).ToString().ToLower(),
                (byte[] data, ref int index) =>
                {
                    byte temp = data[index];
                    index++;
                    return temp != 0;
                },
                (data) =>
                {
                    data = data.RemoveString(" ", "\n", "\r", "\t");
                    if (data == "1") return true;
                    else if (data == "0") return false;
                    return Convert.ToBoolean(data);
                }),
                new SerializationFunc(
                (data) =>
                {
                    byte[] len = GetBytesLength(((bool[])data).Length);
                    if (((bool[])data).Length == 0) return len;
                    byte[] output = new byte[len.Length + (((bool[])data).Length - 1) / 8 + 1];
                    len.CopyTo(output, 0);
                    for (int i = 0, k = len.Length - 1; i < ((bool[])data).Length; i++)
                    {
                        if (i % 8 == 0)
                        {
                            k++;
                            output[k] = 0;
                        }
                        output[k] <<= 1;
                        if (((bool[])data)[i])
                        {
                            output[k]++;
                        }
                    }
                    return output;
                },
                (data) => 
                {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < ((bool[])data).Length; i++)
                    {
                        stringBuilder.Append(((bool[])data)[i] ? "1" : "0");
                    }
                    return stringBuilder.ToString();
                },
                (byte[] data, ref int index) =>
                {
                    int len = GetIntLength(data, ref index);
                    bool[] output = new bool[len];
                    byte nowbools = 0;
                    for (int i = 0; i < len; i++)
                    {
                        if (i % 8 == 0)
                        {
                            nowbools = data[index];
                            index++;
                        }
                        int nowleft = len - (i / 8 * 8) >= 8 ? 8 : len - (i / 8 * 8);
                        if ((i / 8 * 8) + (nowleft - 1 - (i % 8)) < output.Length)
                        {
                            output[(i / 8 * 8) + (nowleft - 1 - (i % 8))] = (nowbools & 1) == 1;
                        }
                        nowbools >>= 1;
                    }
                    return output;
                },
                (data) =>
                {
                    data = data.RemoveString(" ", "\n", "\r", "\t");
                    bool[] output = new bool[data.Length];
                    for (int i = 0; i < data.Length; i++)
                    {
                        output[i] = Convert.ToByte(data[i].ToString()) != 0;
                    }
                    return output;
                })));
            #endregion
            #region char
            typings.Add(new typing(11, typeof(char), "char", false, new SerializationFunc(
                (data) => Encoding.UTF8.GetBytes(new char[1] { (char)data }),
                (data) => "\'" + StringTool.Escape(data.ToString()) + "\'",
                (byte[] data, ref int index) =>
                {
                    int num = 0;
                    byte[] m_charBytes = new byte[128];
                    char[] m_singleChar = new char[1];
                    Decoder decoder = Encoding.UTF8.GetDecoder();
                    while (num == 0)
                    {
                        m_charBytes[0] = data[index++];
                        num = decoder.GetChars(m_charBytes, 0, 1, m_singleChar, 0);
                    }
                    return m_singleChar[0];
                },
                (data) => StringTool.Unescape(data.TakeString('\'', '\'')[0])[0])));
            #endregion
            #region string
            typings.Add(new typing(12, typeof(string), "string", true, new SerializationFunc(
                (data) => 
                {
                    byte[] strdata = Encoding.UTF8.GetBytes((string)data);
                    byte[] strlen = GetBytesLength(strdata.Length);
                    byte[] ans = new byte[strlen.Length + strdata.Length];
                    strlen.CopyTo(ans, 0);
                    strdata.CopyTo(ans, strlen.Length);
                    return ans;
                },
                (data) => "\"" + StringTool.Escape(data.ToString()) + "\"",
                (byte[] data, ref int index) =>
                {
                    int len = GetIntLength(data, ref index);
                    byte[] nowdata = new byte[len];
                    Array.Copy(data, index, nowdata, 0, len);
                    index += len;
                    return Encoding.UTF8.GetString(nowdata);
                },
                (data) => StringTool.Unescape(data.TakeString('\"', '\"')[0]))));
            #endregion
            #region Dictionary
            typings.Add(new typing(13, typeof(IDictionary), new string[] { "dict", "map", "Map", "Dictionary" }, true, new SerializationFunc(
                (data) =>
                {
                    IDictionary nowdata = (IDictionary)data;
                    using (MemoryStream stream = new MemoryStream())
                    using (BinaryWriter writer = new BinaryWriter(stream))
                    {
                        Type[] Subdatatype = nowdata.GetType().GetGenericArguments();
                        int keyrank = ArrayRank(out Type keybasktype, ref Subdatatype[0]),
                            datarank = ArrayRank(out Type databasktype, ref Subdatatype[1]);
                        typing keytyping = this[keybasktype];
                        typing datatyping = this[databasktype];
                        writer.Write(keytyping.index);
                        writer.Write((byte)keyrank);
                        writer.Write(datatyping.index);
                        writer.Write((byte)datarank);
                        writer.Write(GetBytesLength(nowdata.Count));
                        Array keys = Array.CreateInstance(Subdatatype[0], nowdata.Keys.Count);
                        Array values = Array.CreateInstance(Subdatatype[1], nowdata.Values.Count);
                        nowdata.Keys.CopyTo(keys, 0);
                        nowdata.Values.CopyTo(values, 0);
                        using (MemoryStream stream2 = new MemoryStream())
                        using (BinaryWriter writer2 = new BinaryWriter(stream2))
                        {
                            byte valuenullbool = 0;
                            for (int i = 0; i < nowdata.Count; i++)
                            {
                                object key = keys.GetValue(i);
                                ArrayRank(out Type keybasetype, ref key);
                                object value = values.GetValue(i);
                                ArrayRank(out Type valuebasetype, ref value);
                                byte[] keydata = ChuonBinary.Typing(keytyping, key, keyrank);
                                byte[] valuedata = ChuonBinary.Typing(datatyping, value, datarank);

                                if ((datarank > 0 || datatyping.cannull) && nowdata.Count > 0)
                                {
                                    if (i % 8 == 0)
                                    {
                                        if (i != 0)
                                        {
                                            writer.Write(valuenullbool);
                                        }
                                        valuenullbool = 0;
                                    }
                                    valuenullbool <<= 1;
                                    if (valuedata == null)
                                    {
                                        valuenullbool++;
                                    }
                                }
                                writer2.Write(keydata, 0, keydata.Length);
                                if (valuedata != null)
                                {
                                    writer2.Write(valuedata, 0, valuedata.Length);
                                }
                            }
                            if ((datarank > 0 || datatyping.cannull) && nowdata.Count > 0) writer.Write(valuenullbool);
                            writer2.Close();
                            stream2.Close();
                            writer.Write(stream2.ToArray());
                        }
                        writer.Close();
                        stream.Close();
                        return stream.ToArray();
                    }
                },
                (data) =>
                {
                    IDictionary nowdata = (IDictionary)data;
                    StringBuilder stringBuilder = new StringBuilder();

                    Type[] Subdatatype = nowdata.GetType().GetGenericArguments();
                    int keyrank = ArrayRank(out Type keybasktype, ref Subdatatype[0]),
                        datarank = ArrayRank(out Type databasktype, ref Subdatatype[1]);
                    typing keytyping = this[keybasktype];
                    typing datatyping = this[databasktype];
                    stringBuilder.Append("{");
                    stringBuilder.Append(keytyping.name);
                    for (int i = 0; i < keyrank; i++)
                    {
                        stringBuilder.Append("[]");
                    }
                    stringBuilder.Append(":");
                    stringBuilder.Append(datatyping.name);
                    for (int i = 0; i < datarank; i++)
                    {
                        stringBuilder.Append("[]");
                    }
                    stringBuilder.Append(":");
                    Array keys = Array.CreateInstance(Subdatatype[0], nowdata.Keys.Count);
                    Array values = Array.CreateInstance(Subdatatype[1], nowdata.Values.Count);
                    nowdata.Keys.CopyTo(keys, 0);
                    nowdata.Values.CopyTo(values, 0);
                    for (int i = 0; i < nowdata.Count; i++)
                    {
                        stringBuilder.Append("{");
                        object key = keys.GetValue(i);
                        ArrayRank(out Type keybasetype, ref key);
                        object value = values.GetValue(i);
                        ArrayRank(out Type valuebasetype, ref value);
                        stringBuilder.Append(ChuonString.Typing(keytyping, key, keyrank));
                        stringBuilder.Append(",");
                        stringBuilder.Append(ChuonString.Typing(datatyping, value, datarank));
                        stringBuilder.Append("}");
                    }
                    stringBuilder.Append("}");
                    return stringBuilder.ToString();
                },
                (byte[] data, ref int index) =>
                {
                    typing keytyping = this[data[index++]];
                    int keyrank = data[index++];
                    typing datatyping = this[data[index++]];
                    int datarank = data[index++];
                    int len = GetIntLength(data, ref index);

                    Type keynowtype = keytyping.type;
                    for (int i = 0; i < keyrank; i++)
                    {
                        keynowtype = keynowtype.MakeArrayType();
                    }

                    Type datanowtype = datatyping.type;
                    for (int i = 0; i < datarank; i++)
                    {
                        datanowtype = datanowtype.MakeArrayType();
                    }

                    Type thistype = typeof(Dictionary<,>).MakeGenericType(new Type[] { keynowtype, datanowtype });
                    System.Reflection.MethodInfo method = thistype.GetMethod("Add");

                    IDictionary ans = (IDictionary)Activator.CreateInstance(thistype);

                    bool[] valueallnullbools = new bool[len];
                    if (datarank > 0 || datatyping.cannull)
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
                            if ((i / 8 * 8) + (nowleft - 1 - (i % 8)) < valueallnullbools.Length)
                            {
                                valueallnullbools[(i / 8 * 8) + (nowleft - 1 - (i % 8))] = (nullbool & 1) == 1;
                            }
                            nullbool >>= 1;
                        }
                    }
                    for (int i = 0; i < len; i++)
                    {
                        object key = ChuonBinary.GetTyp(keytyping, data, keyrank, ref index);
                        object value = valueallnullbools[i] ? null : ChuonBinary.GetTyp(datatyping, data, datarank, ref index);
                        method.Invoke(ans, new object[] { key, value });
                    }
                    return ans;
                },
                (data) =>
                {
                    string[] typeanddata = StringTool.SplitWithFormatWithoutinArray(data.TakeString('{', '}')[0], ':');
                    typeanddata[0] = typeanddata[0].RemoveString(" ", "\n", "\r", "\t");
                    typeanddata[1] = typeanddata[1].RemoveString(" ", "\n", "\r", "\t");
                    typing keytyping = this[typeanddata[0].RemoveString("[]")];
                    int keyrank = typeanddata[0].TakeString('[', ']').Length;
                    typing datatyping = this[typeanddata[1].RemoveString("[]")];
                    int datarank = typeanddata[1].TakeString('[', ']').Length;
                    string[] alldata = typeanddata[2].TakeString('{', '}');
                    int len = alldata.Length;

                    Type keynowtype = keytyping.type;
                    for (int i = 0; i < keyrank; i++)
                    {
                        keynowtype = keynowtype.MakeArrayType();
                    }

                    Type datanowtype = datatyping.type;
                    for (int i = 0; i < datarank; i++)
                    {
                        datanowtype = datanowtype.MakeArrayType();
                    }

                    Type thistype = typeof(Dictionary<,>).MakeGenericType(new Type[] { keynowtype, datanowtype });
                    System.Reflection.MethodInfo method = thistype.GetMethod("Add");

                    IDictionary ans = (IDictionary)Activator.CreateInstance(thistype);
                    for (int i = 0; i < len; i++)
                    {
                        string[] nowdata = StringTool.SplitWithFormatWithoutinArray(alldata[i], ',');
                        object key = ChuonString.GetTyp(keytyping, keyrank, nowdata[0]);
                        object value = ChuonString.GetTyp(datatyping, datarank, nowdata[1]);
                        method.Invoke(ans, new object[] { key, value });
                    }
                    return ans;
                })));
            #endregion
        }
    }
}
