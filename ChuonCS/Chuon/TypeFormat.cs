using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Chuon
{
    static class TypeFormat
    {
        #region TypeName
        public static readonly string[] type = new string[] { "Byte[]", "SByte[]", "Int16[]", "Int32[]", "Int64[]", "UInt16[]", "UInt32[]", "UInt64[]", "Single[]", "Double[]", "Decimal[]", "Char[]", "String[]", "Boolean[]", "Object[]", "Dictionary`2", "Byte", "SByte", "Int16", "Int32", "Int64", "UInt16", "UInt32", "UInt64", "Single", "Double", "Decimal", "Char", "String", "Boolean", "Object", "null" };
        public static readonly string[] typelist = new string[] { "byte[]", "sbyte[]", "short[]", "int[]", "long[]", "ushort[]", "uint[]", "ulong[]", "float[]", "double[]", "decimal[]", "char[]", "string[]", "bool[]", "object[]", "Dictionary", "byte", "sbyte", "short", "int", "long", "ushort", "uint", "ulong", "float", "double", "decimal", "char", "string", "bool", "object" };
        public static readonly string[] typelist2 = new string[] { "Byte[]", "SByte[]", "Int16[]", "Int32[]", "Int64[]", "UInt16[]", "UInt32[]", "UInt64[]", "Single[]", "Double[]", "Decimal[]", "Char[]", "String[]", "Boolean[]", "Object[]", "Dictionary`2", "Byte", "SByte", "Int16", "Int32", "Int64", "UInt16", "UInt32", "UInt64", "Single", "Double", "Decimal", "Char", "String", "Boolean", "Object" };

        public static string ToTrueTypeName(string type)
        {
            string typenames = type;
            if (Array.IndexOf(typelist2, type) == -1 && type != "null")
            {
                typenames = typelist2[Array.IndexOf(typelist, type)];
            }
            return typenames;
        }

        public static string ToSimpleTypeName(string type)
        {
            string typenames = type;
            if (Array.IndexOf(typelist, type) == -1 && type != "null")
            {
                typenames = typelist[Array.IndexOf(typelist2, type)];
            }
            return typenames;
        }

        public static Type TypeNameToType(string typename)
        {
            return Type.GetType((typename == "Dictionary`2" ? "System.Collections.Generic." : "System.") + typename);
        }
        #endregion
    }
}
