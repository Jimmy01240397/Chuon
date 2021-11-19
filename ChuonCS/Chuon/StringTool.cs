using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Chuon
{
    public static class StringTool
    {
        readonly static char[] format = { '\t', '\n', '\r', '\f', '\'', '\"', '\\', '{', '}', '[', ']', ',', ':' };
        readonly static char[] unformat = { 't', 'n', 'r', 'f' };

        #region 字串處理
        static int Matches(string input, char a)
        {
            string[] j = SplitWithFormat(input, a);
            return j.Length + 1;
        }

        public static string Escape(string input)
        {
            for (int i = 0; i < input.Length; i++)
            {
                if (IsMetachar(input[i]))
                {
                    StringBuilder stringBuilder = new StringBuilder();
                    char c = input[i];
                    stringBuilder.Append(input, 0, i);
                    do
                    {
                        stringBuilder.Append('\\');
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
                        stringBuilder.Append(c);
                        i++;
                        int num = i;
                        while (i < input.Length)
                        {
                            c = input[i];
                            if (IsMetachar(c))
                            {
                                break;
                            }
                            i++;
                        }
                        stringBuilder.Append(input, num, i - num);
                    }
                    while (i < input.Length);
                    return stringBuilder.ToString();
                }
            }
            return input;
        }

        public static string Unescape(string input)
        {
            StringBuilder stringBuilder = new StringBuilder(input);
            for (int i = 0; i < stringBuilder.Length; i++)
            {
                if (stringBuilder[i] == '\\')
                {
                    stringBuilder.Remove(i, 1);
                    if (Array.IndexOf(unformat, stringBuilder[i]) != -1)
                    {
                        stringBuilder[i] = format[Array.IndexOf(unformat, stringBuilder[i])];
                    }
                }
            }
            return stringBuilder.ToString();
        }

        static bool IsMetachar(char ch)
        {
            return Array.IndexOf(format, ch) != -1;
        }

        public static string[] SplitWithFormat(string input, char a)
        {
            List<string> vs = new List<string>();
            int now = 0;
            for (int i = 0; i < input.Length; i++)
            {
                if (input[i] == '\\')
                {
                    i++;
                }
                else if (input[i] == a)
                {
                    vs.Add(input.Substring(now, i - now));
                    now = i + 1;
                }
            }
            vs.Add(input.Substring(now, input.Length - now));
            return vs.ToArray();
        }
        #endregion

        #region Hex
        static public byte[] HexToBytes(string str)
        {
            str = str.RemoveString(" ", "\n", "\r", "\t");
            byte[] bytes = new byte[str.Length / 2];
            int j = 0;

            byte HexToByte(string hex)
            {
                if (hex.Length > 2 || hex.Length <= 0)
                    throw new ArgumentException("hex must be 1 or 2 characters in length");
                byte newByte = byte.Parse(hex, System.Globalization.NumberStyles.HexNumber);
                return newByte;
            }

            for (int i = 0; i < bytes.Length; i++)
            {
                string hex = new String(new Char[] { str[j], str[j + 1] });
                bytes[i] = HexToByte(hex);
                j = j + 2;
            }
            return bytes;
        }

        static public string BytesToHex(byte[] bytes)
        {
            StringBuilder str2 = new StringBuilder();
            for (int i = 0; i < bytes.Length; i++)
            {
                str2.Append(bytes[i].ToString("X2"));
            }
            return str2.ToString();
        }
        #endregion

        #region 擴充方法
        /// <summary>
        /// take a string between charA and charB
        /// </summary>
        /// <param name="text">parent string</param>
        /// <param name="a">left char</param>
        /// <param name="b">right char</param>
        /// <returns></returns>
        public static string[] TakeString(this string text, char a, char b)
        {
            List<string> q = new List<string>(SplitWithFormat(text, b));
            if (a == b)
            {
                if (q.Count % 2 == 0)
                {
                    q.RemoveAt(q.Count - 1);
                }
                for (int i = 0; i < q.Count; i++)
                {
                    q.RemoveAt(i);
                }
                return q.ToArray();
            }
            q.RemoveAt(q.Count - 1);
            for (int i = 0; i < q.Count;)
            {
                if (q[i] != "")
                {
                    if (Matches(q[i], a) != Matches(q[i], b) + 1)
                    {
                        q[i] += b.ToString() + q[i + 1];
                        q.RemoveAt(i + 1);
                    }
                    else
                    {
                        i++;
                    }
                }
                else
                {
                    q[i - 1] += b.ToString();
                    q.RemoveAt(i);
                }
            }
            List<string> vs = new List<string>();
            foreach (string s in q)
            {
                int found = 0;
                for (int i = 0; i < s.Length; i++)
                {
                    if (s[i] == '\\')
                    {
                        i++;
                    }
                    else if (s[i] == a)
                    {
                        found = i;
                        break;
                    }
                }
                if (found != -1)
                {
                    if (found + 1 == s.Length)
                    {
                        vs.Add("");
                    }
                    else
                    {
                        vs.Add(s.Substring(found + 1));
                    }
                }
            }
            return vs.ToArray();
        }

        /// <summary>
        /// remove string from input
        /// </summary>
        /// <param name="input">parent string</param>
        /// <param name="arg">string need remove</param>
        /// <returns></returns>
        public static string RemoveString(this string input, params string[] arg)
        {
            for (int i = 0; i < arg.Length; i++)
            {
                input = input.Replace(arg[i], "");
            }
            return input;
        }
        #endregion
    }
}
