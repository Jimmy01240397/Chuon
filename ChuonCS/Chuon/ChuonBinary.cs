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
            using (MemoryStream stream = new MemoryStream())
            using (BinaryWriter writer = new BinaryWriter(stream))
            {
                data = Typing(writer, thing);
                writer.Close();
                stream.Close();
            }
        }

        public ChuonBinary(byte[] thing)
        {
            data = new byte[thing.Length];
            thing.CopyTo(data, 0);
            ToObject();
        }

        public object ToObject()
        {
            object output = null;
            if (data != null)
            {
                using (MemoryStream stream = new MemoryStream(data))
                using (BinaryReader reader = new BinaryReader(stream))
                {
                    output = GetTyp(reader);
                    reader.Close();
                    stream.Close();
                }
            }
            return output;
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
            object datas = ToObject();
            return new ChuonString(datas);
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

        public static int GetIntLength(BinaryReader reader)
        {
            List<byte> vs = new List<byte>();
            byte a;
            do
            {
                a = reader.ReadByte();
                vs.Add((byte)(a % 128));
            } while (a >= 128);
            int x = 0;
            for (int i = 0; i < vs.Count; i++)
            {
                x += (int)(vs[i] * Math.Pow(128, i));
            }
            return x;
        }
        #endregion

        #region Typing
        static void TypingArray(BinaryWriter writer, object thing)
        {
            Array c = (Array)thing;
            string typename = thing.GetType().Name;
            writer.Write((byte)(Array.IndexOf(TypeFormat.type, typename)));
            writer.Write(GetBytesLength(c.Length));
            typename = typename.RemoveString("[", "]");
            if (typename == "Byte" || typename == "Char")
            {
                typename += "[]";
                System.Reflection.MethodInfo write = typeof(BinaryWriter).GetMethod("Write", new Type[] { TypeFormat.TypeNameToType(typename) });
                write.Invoke(writer, new object[] { c });
            }
            else
            {
                System.Reflection.MethodInfo write = typeof(BinaryWriter).GetMethod("Write", new Type[] { TypeFormat.TypeNameToType(typename) });
                for (int ii = 0; ii < c.Length; ii++)
                {
                    if (typename == "Object")
                    {
                        Typing(writer, c.GetValue(ii));
                    }
                    else
                    {
                        write.Invoke(writer, new object[] { c.GetValue(ii) });
                    }
                }
            }
        }

        static void TypingNotArray(BinaryWriter writer, object thing)
        {
            string typename = thing.GetType().Name;
            writer.Write((byte)(Array.IndexOf(TypeFormat.type, typename)));
            System.Reflection.MethodInfo write = typeof(BinaryWriter).GetMethod("Write", new Type[] { TypeFormat.TypeNameToType(typename) });
            write.Invoke(writer, new object[] { thing });
        }

        static byte[] Typing(BinaryWriter writer, object thing)
        {
            if (thing != null)
            {
                if (thing.GetType().Name.Contains("[]"))
                {
                    TypingArray(writer, thing);
                }
                else if (thing.GetType().Name == "Dictionary`2")
                {
                    Type datatype = thing.GetType();
                    Type[] Subdatatype = datatype.GetGenericArguments();
                    IDictionary c = (IDictionary)thing;
                    writer.Write((byte)(Array.IndexOf(TypeFormat.type, datatype.Name)));
                    writer.Write((byte)(Array.IndexOf(TypeFormat.type, Subdatatype[0].Name)));
                    writer.Write((byte)(Array.IndexOf(TypeFormat.type, Subdatatype[1].Name)));
                    writer.Write(GetBytesLength(c.Count));

                    Array keys = Array.CreateInstance(Subdatatype[0], c.Keys.Count);
                    Array values = Array.CreateInstance(Subdatatype[1], c.Values.Count);
                    c.Keys.CopyTo(keys, 0);
                    c.Values.CopyTo(values, 0);
                    for (int i = 0; i < c.Count; i++)
                    {
                        Typing(writer, keys.GetValue(i));
                        Typing(writer, values.GetValue(i));
                    }
                }
                else if (Array.IndexOf(TypeFormat.type, thing.GetType().Name) != -1)
                {
                    TypingNotArray(writer, thing);
                }
                else
                {
                    writer.Write((byte)TypeFormat.type.Length);
                    writer.Write(thing.ToString());
                }
            }
            else
            {
                writer.Write((byte)(Array.IndexOf(TypeFormat.type, "null")));
                writer.Write(false);
            }
            MemoryStream stream = (MemoryStream)writer.BaseStream;
            return stream.ToArray();
        }
        #endregion

        #region GetTyp
        static object GetTypArray(string typ, BinaryReader reader)
        {
            int count = GetIntLength(reader);
            typ = typ.RemoveString("[", "]");
            if (typ == "Byte" || typ == "Char")
            {
                System.Reflection.MethodInfo method = typeof(BinaryReader).GetMethod("Read" + TypeFormat.TypeNameToType(typ).Name + "s");
                return method.Invoke(reader, new object[] { count });
            }
            else
            {
                Array d = Array.CreateInstance(TypeFormat.TypeNameToType(typ), count);
                System.Reflection.MethodInfo method = typeof(BinaryReader).GetMethod("Read" + TypeFormat.TypeNameToType(typ).Name);
                for (int i = 0; i < d.Length; i++)
                {
                    if (typ == "Object")
                    {
                        d.SetValue(GetTyp(reader), i);
                    }
                    else
                    {
                        d.SetValue(method.Invoke(reader, null), i);
                    }
                }
                return d;
            }
        }

        static object GetTyp(BinaryReader reader)
        {
            byte data = reader.ReadByte();
            object get;
            if (data < TypeFormat.type.Length)
            {
                string typ = TypeFormat.type[data];
                if (typ.Contains("[]"))
                {
                    get = GetTypArray(typ, reader);
                }
                else if (typ == "Dictionary`2")
                {
                    string[] typenames = new string[] { TypeFormat.type[reader.ReadByte()], TypeFormat.type[reader.ReadByte()] };
                    Type[] types = new Type[] { TypeFormat.TypeNameToType(typenames[0]), TypeFormat.TypeNameToType(typenames[1]) };

                    Type thistype = typeof(Dictionary<,>).MakeGenericType(types);
                    System.Reflection.MethodInfo method = thistype.GetMethod("Add");

                    IDictionary d = (IDictionary)Activator.CreateInstance(thistype);
                    int count = GetIntLength(reader);
                    for (int ii = 0; ii < count; ii++)
                    {
                        object key = GetTyp(reader);
                        object value = GetTyp(reader);
                        method.Invoke(d, new object[] { key, value });
                    }
                    get = d;
                }
                else if (typ == "null")
                {
                    bool a = reader.ReadBoolean();
                    get = null;
                }
                else if (Array.IndexOf(TypeFormat.type, typ) != -1)
                {
                    System.Reflection.MethodInfo method = typeof(BinaryReader).GetMethod("Read" + TypeFormat.TypeNameToType(typ).Name);
                    get = method.Invoke(reader, null);
                }
                else
                {
                    get = typ;
                }
            }
            else
            {
                get = reader.ReadString();
            }
            return get;
        }
        #endregion
    }
}
