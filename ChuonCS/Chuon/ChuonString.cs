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
            data = ObjectToString(0, thing, false);
        }

        public ChuonString(string thing)
        {
            data = thing;
            ToObject();
        }

        public ChuonString(byte[] thing, Encoding encoding)
        {
            data = encoding.GetString(thing);
            ToObject();
        }

        public object ToObject()
        {
            return ChuonStringDeserializeToObject(data);
        }

        public override string ToString()
        {
            return data;
        }

        public string ToStringWithEnter()
        {
            return ObjectSerializeToChuonStringWithEnter(ToObject());
        }

        public byte[] ToBinaryArray(Encoding encoding)
        {
            return encoding.GetBytes(data);
        }
        public ChuonBinary ToChuonBinary()
        {
            object datas = ToObject();
            return new ChuonBinary(datas);
        }

        #region ObjectToString
        static string ObjectToStringForArray(int cont, object thing, bool enter)
        {
            Array c = (Array)thing;
            string type = TypeFormat.typelist[Array.IndexOf(TypeFormat.typelist2, thing.GetType().Name)];
            string a = "";
            if (c.Length > 0)
            {
                a += printTab(enter, cont) + "{" + printTab(enter, cont + 1);
                if (type == "byte[]")
                {
                    a += StringTool.BytesToHex((byte[])c) + printTab(enter, cont) + "}";
                }
                else
                {
                    string makestring(string leftright, bool isstringorchar, int index)
                    {
                        if (isstringorchar)
                            return leftright + BeforeFormatString(c.GetValue(index).ToString(), new char[] { '\'', '\"', '{', '}', '[', ']', ',', ':' }) + leftright;
                        else
                            return c.GetValue(index).ToString();
                    }
                    for (int i = 0; i < c.Length - 1; i++)
                    {
                        if (type == "object[]")
                            a += ObjectToString(cont + 1, c.GetValue(i), enter) + "," + printTab(enter, cont + 1);
                        else
                            a += makestring(type == "char[]" ? "\'" : "\"", type == "char[]" || type == "string[]", i) + ",";
                    }
                    if (type == "object[]")
                        a += ObjectToString(cont + 1, c.GetValue(c.Length - 1), enter) + printTab(enter, cont) + "}";
                    else
                        a += makestring(type == "char[]" ? "\'" : "\"", type == "char[]" || type == "string[]", c.Length - 1) + "}";
                }
            }
            else
            {
                a += "NotThing";
            }
            return a;
        }

        static string ObjectToString(int cont, object thing, bool enter)
        {
            string a = "";
            if (thing != null)
            {
                string typ = TypeFormat.typelist[Array.IndexOf(TypeFormat.typelist2, thing.GetType().Name)];
                a += typ + ":";
                if (typ.Contains("[]"))
                {
                    a += ObjectToStringForArray(cont, thing, enter);
                }
                else if (typ == "Dictionary")
                {
                    Type datatype = thing.GetType();
                    Type[] Subdatatype = datatype.GetGenericArguments();
                    IDictionary c = (IDictionary)thing;
                    a += printTab(enter, cont) + "{" + printTab(enter, cont + 1) + TypeFormat.typelist[Array.IndexOf(TypeFormat.typelist2, Subdatatype[0].Name)] + ":" + TypeFormat.typelist[Array.IndexOf(TypeFormat.typelist2, Subdatatype[1].Name)] + ":";

                    if (c.Count > 0)
                    {
                        Array keys = Array.CreateInstance(Subdatatype[0], c.Keys.Count);
                        Array values = Array.CreateInstance(Subdatatype[1], c.Values.Count);
                        c.Keys.CopyTo(keys, 0);
                        c.Values.CopyTo(values, 0);
                        for (int i = 0; i < c.Count; i++)
                        {
                            a += printTab(enter, cont + 1) + "{" + printTab(enter, cont + 2) + ObjectToString(cont + 2, keys.GetValue(i), enter) + "," + printTab(enter, cont + 2) + ObjectToString(cont + 2, values.GetValue(i), enter) + printTab(enter, cont + 1) + "}";
                        }
                    }
                    else
                    {
                        a += "NotThing";
                    }
                    a += printTab(enter, cont) + "}";
                }
                else if (Array.IndexOf(TypeFormat.typelist, typ) != -1)
                {
                    string makestring(string leftright, bool isstringorchar)
                    {
                        if (isstringorchar)
                            return leftright + BeforeFormatString(thing.ToString(), new char[] { '\'', '\"', '{', '}', '[', ']', ',', ':' }) + leftright;
                        else
                            return thing.ToString();
                    }
                    a += makestring(typ == "char" ? "\'" : "\"", typ == "char" || typ == "string");
                }
                else
                {
                    a += thing.ToString();
                }
            }
            else
            {
                a += "null";
            }
            return a;
        }

        /// <summary>
        /// object Format to SerializationData Text
        /// </summary>
        /// <param name="thing">object need to Format</param>
        /// <returns>SerializationData Text</returns>
        static string ObjectSerializeToChuonString(object thing)
        {
            return ObjectToString(0, thing, false);
        }

        /// <summary>
        /// object Format to SerializationData Text with Wrap
        /// </summary>
        /// <param name="thing">object need to Format</param>
        /// <returns>SerializationData Text</returns>
        static string ObjectSerializeToChuonStringWithEnter(object thing)
        {
            return ObjectToString(0, thing, true);
        }
        #endregion

        #region StringToObject
        static object StringToObjectForArray(string thing)
        {
            string[] vs = StringTool.SplitWithFormat(thing, ':');
            string typ = vs[0].RemoveString(" ", "\n", "\r", "\t", "[", "]");

            string typenames = TypeFormat.ToTrueTypeName(typ);
            Type[] types = new Type[] { TypeFormat.TypeNameToType(typenames) };

            int found = thing.IndexOf(':');
            if (thing.Substring(found + 1) != "NotThing")
            {
                string a = thing.Substring(found + 1).TakeString('{', '}')[0];

                if (typ == "byte")
                {
                    a = a.Replace(" ", "");
                    return StringTool.HexToBytes(a);
                }
                else
                {
                    string[] b = null;
                    if (typ == "object")
                    {
                        b = a.TakeString('{', '}');
                        for (int i = 0; i < b.Length; i++)
                        {
                            int index = a.IndexOf("{" + b[i] + "}");
                            a = a.Substring(0, index) + "[" + i + "]" + a.Substring(index + b[i].Length + 2);
                        }
                        string[] bb = StringTool.SplitWithFormat(a, ',');
                        for (int i = 0; i < bb.Length; i++)
                        {
                            for (int ii = 0; ii < b.Length; ii++)
                            {
                                bb[i] = bb[i].Replace("[" + ii + "]", "{" + b[ii] + "}");
                            }
                        }
                        b = bb;
                    }
                    else
                    {
                        b = StringTool.SplitWithFormat(a, ',');
                    }

                    Type thistype = typeof(List<>).MakeGenericType(types);
                    IList c = (IList)Activator.CreateInstance(thistype);
                    System.Reflection.MethodInfo toarray = thistype.GetMethod("ToArray");

                    if (typ == "object")
                    {
                        for (int i = 0; i < b.Length; i++)
                        {
                            c.Add(ChuonStringDeserializeToObject(b[i]));
                        }
                    }
                    else
                    {
                        for (int i = 0; i < b.Length; i++)
                        {
                            object[] data = new object[] { b[i] };
                            switch (typ)
                            {
                                case "char":
                                    {
                                        data[0] = b[i].TakeString('\'', '\'')[0];
                                        break;
                                    }
                                case "string":
                                    {
                                        data[0] = b[i].TakeString('\"', '\"')[0];
                                        break;
                                    }
                                case "bool":
                                    {
                                        data[0] = b[i].Replace(" ", "");
                                        break;
                                    }
                            }
                            System.Reflection.MethodInfo method = typeof(Convert).GetMethod("To" + types[0].Name);
                            c.Add(method.Invoke(null, data));
                        }
                    }
                    return toarray.Invoke(c, null);
                }
            }
            else
            {
                return Array.CreateInstance(types[0], 0);
            }
        }

        static object StringToObjectForNotArray(string thing)
        {
            string[] vs = StringTool.SplitWithFormat(thing, ':');
            string typ = vs[0].RemoveString(" ", "\n", "\r", "\t", "[", "]");

            string typenames = TypeFormat.ToTrueTypeName(typ);

            int found = thing.IndexOf(':');
            string a = thing.Substring(found + 1);
            switch (typ)
            {
                case "char":
                    {
                        a = a.TakeString('\'', '\'')[0];
                        break;
                    }
                case "string":
                    {
                        a = a.TakeString('\"', '\"')[0];
                        break;
                    }
                case "bool":
                    {
                        a = a.Replace(" ", "");
                        break;
                    }
            }
            System.Reflection.MethodInfo method = typeof(Convert).GetMethod("To" + TypeFormat.TypeNameToType(typenames).Name, new Type[] { typeof(string) });
            object[] data = new object[] { a };
            object get = method.Invoke(null, data);
            if (typ == "byte")
            {
                get = (byte)get;
            }
            return get;
        }

        /// <summary>
        /// SerializationData Text to object
        /// </summary>
        /// <param name="thing">SerializationData Text</param>
        /// <returns>object</returns>
        static object ChuonStringDeserializeToObject(string thing)
        {
            string[] vs = StringTool.SplitWithFormat(thing, ':');
            string typ = TypeFormat.ToSimpleTypeName(vs[0].RemoveString(" ", "\n", "\r", "\t"));
            object get;
            if (typ.Contains("[]"))
            {
                get = StringToObjectForArray(thing);
            }
            else if (typ == "Dictionary")
            {
                int found = thing.IndexOf(':');
                string _data = thing.Substring(found + 1).TakeString('{', '}')[0];

                int data_index = _data.IndexOf(':');
                int data_index2 = _data.IndexOf(':', data_index + 1);

                string[] data = new string[] { _data.Substring(0, data_index), _data.Substring(data_index + 1, data_index2 - data_index - 1), _data.Substring(data_index2 + 1) };
                data[0] = data[0].RemoveString(" ", "\n", "\r", "\t");
                data[1] = data[1].RemoveString(" ", "\n", "\r", "\t");
                string[] typenames = new string[] { TypeFormat.typelist2[Array.IndexOf(TypeFormat.typelist, data[0])], TypeFormat.typelist2[Array.IndexOf(TypeFormat.typelist, data[1])] };
                Type[] types = new Type[] { TypeFormat.TypeNameToType(typenames[0]), TypeFormat.TypeNameToType(typenames[1]) };

                Type thistype = typeof(Dictionary<,>).MakeGenericType(types);

                get = Activator.CreateInstance(thistype);

                if (data[2] != "NotThing")
                {
                    System.Reflection.MethodInfo method = thistype.GetMethod("Add");

                    string[] a = data[2].TakeString('{', '}');

                    for (int i = 0; i < a.Length; i++)
                    {
                        string[] b = a[i].TakeString('{', '}');
                        for (int ii = 0; ii < b.Length; ii++)
                        {
                            int index = a[i].IndexOf("{" + b[ii] + "}");
                            a[i] = a[i].Substring(0, index) + "[" + ii + "]" + a[i].Substring(index + b[ii].Length + 2);
                        }
                        string[] nowdata = StringTool.SplitWithFormat(a[i], ',');
                        for (int ii = 0; ii < nowdata.Length; ii++)
                        {
                            for (int iii = 0; iii < b.Length; iii++)
                            {
                                nowdata[ii] = nowdata[ii].Replace("[" + iii + "]", "{" + b[iii] + "}");
                            }
                        }
                        object key = ChuonStringDeserializeToObject(nowdata[0]);
                        object value = ChuonStringDeserializeToObject(nowdata[1]);
                        method.Invoke(get, new object[] { key, value });
                    }
                }
            }
            else if (typ == "null")
            {
                get = null;
            }
            else if (Array.IndexOf(TypeFormat.typelist, typ) != -1)
            {
                get = StringToObjectForNotArray(thing);
            }
            else
            {
                get = typ;
            }
            return get;
        }
        #endregion


        static string BeforeFormatString(string input, char[] a)
        {
            StringBuilder stringBuilder = new StringBuilder(input);
            for (int i = 0; i < stringBuilder.Length; i++)
            {
                if (Array.IndexOf(a, stringBuilder[i]) != -1 || stringBuilder[i] == '\\')
                {
                    stringBuilder.Insert(i, "\\");
                    i++;
                }
            }
            return stringBuilder.ToString();
        }

        static string printTab(bool enable, int cont)
        {
            if (!enable)
            {
                return "";
            }
            string ans = "\r\n";
            for (int i = 0; i < cont; i++)
            {
                ans += "\t";
            }
            return ans;
        }

    }
}
