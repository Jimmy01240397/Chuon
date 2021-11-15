# Chuon
Chummy Object Notation is a data interchange format and a serialization format that uses human-readable text to store and transmit data objects consisting of attribute-value pairs and arrays (or other serializable values). 
Or it can use the most suitable serialization format to serialize data objects consisting of attribute-value pairs and arrays (or other serializable values).

* [Binary Format](#binary-format)
  * [Base 128](#base-128)
  * [data binary length](#data-binary-length)
* [String Format](#string-format)
  * [byte](#byte)
  * [sbyte](#sbyte)
  * [short](#short)
  * [int](#int)
  * [long](#long)
  * [ushort](#ushort)
  * [uint](#uint)
  * [ulong](#ulong)
  * [float](#float)
  * [double](#double)
  * [decimal](#decimal)
  * [char](#char)
  * [string](#string)
  * [bool](#bool)
  * [object](#object)
  * [Dictionary](#dictionary)
* [Usage](#usage)
* [Example](#example)

## Binary Format

| type | length(if it is Array) | data |
| -------- | -------- | -------- |
| 1 byte     | num is Base 128       | data binary length |

### Base 128
If first byte is Less than 128 than it don't have next digit.

If first byte is Greater than 128 than the number is:

(byte[0]-128) + (byte[1]-128) * 128^1 + ... + (byte[n - 1]-128) * 128^{n - 1} + byte[n] * 128^n

![](https://render.githubusercontent.com/render/math?math=(byte%5B0%5D-128)%20%2B%20(byte%5B1%5D-128)%20*%20128%5E1%20%2B%20...%20%2B%20(byte%5Bn%20-%201%5D-128)%20*%20128%5E%7Bn%20-%201%7D%20%2B%20byte%5Bn%5D%20*%20128%5En)

### data binary length


| type | length |
| -------- | -------- |
| byte     | 1 byte     |
| sbyte     | 1 byte     |
| short     | 2 bytes     |
| int     | 4 bytes     |
| long     | 8 bytes     |
| ushort     | 2 bytes     |
| uint     | 4 bytes     |
| ulong     | 8 bytes     |
| float     | 4 bytes     |
| double     | 8 bytes     |
| decimal     | 16 bytes     |
| char     | 1 byte     |
| string     | string_length_length + string_length bytes     |
| bool     | 1 byte     |
| object     | data_type_length + data_length_length(if it is Array) + data_length bytes    |
| Dictionary     | key_type_length + data_type_length + length_length + (key_type_length + length(if it is Array) + key_length + data_type_length + length(if it is Array) + data_length)... bytes     |

## String Format
There are the types that you can use.
* byte 
* sbyte 
* short 
* int
* long
* ushort
* uint
* ulong
* float
* double
* decimal
* char
* string
* bool
* object[]
* Dictionary

Arrays can be used except for Dictionary and object[](it is already an array)

### byte
``` C
byte:<number>
```

#### Array
``` C
byte[]:
{
	<Hex(don't add 0x)>
}
```
### sbyte
``` C
sbyte:<number between -128~127>
```
#### Array
``` C
sbyte[]:
{
	-55,
	22,
	100,
    <number between -128~127>
}
```
### short
``` C
short:<number>
```
#### Array
``` C
short[]:
{
	10000,
	-20000
}
```
### int
``` C
int:<number>
```
#### Array
``` C
int[]:
{
	100000000,
	-200000000
}
```
### long
``` C
long:<number>
```
#### Array
``` C
long[]:
{
	1000000000000000000,
	-200000000000000000
}
```
### ushort
``` C
ushort:<number>
```
#### Array
``` C
ushort[]:
{
	10000,
	20000
}
```
### uint
``` C
uint:<number>
```
#### Array
``` C
uint[]:
{
	100000000,
	200000000
}
```
### ulong
``` C
ulong:<number>
```
#### Array
``` C
ulong[]:
{
	1000000000000000000,
	2000000000000000000
}
```
### float
``` C
float:<floating-point number>
```
#### Array
``` C
float[]:
{
	5.5555,
	-3.14
}
```
### double
``` C
double:<floating-point number>
```
#### Array
``` C
double[]:
{
	5.55555555555,
	-3.1415926
}
```
### decimal
``` C
decimal:<floating-point number>
```
#### Array
``` C
decimal[]:
{
	5.55555555555,
	-3.1415926
}
```
### char
``` C
char:'<character>'
```
#### Array
``` C
char[]:
{
	'a',
	'n'
}
```
### string
``` C
string:"<string>"
```
#### Array
``` C
string[]:
{
	"Hello World",
	"coming soon"
}
```
### bool
``` C
bool:<boolean>
```
#### Array
``` C
bool[]:
{
	true,
	false
}
```
### object[]
``` C
object[]:
{
    int:55,
    string[]:
    {
        "Hello World",
        "aaaaa"
    },
    <type>:<data>
}
```
### Dictionary
``` C
Dictionary:
{
    <key type>:<data type>:
    {
        <key type>:<key>,
        <data type>:<data>
    }
    {
        string:"now",
        int:111
    }
}
```

## Usage
### use Chuon
#### C#
``` C#
using Chuon;
```
#### Java
``` java
import com.jimmiker.ChuonString;
import com.jimmiker.ChuonBinary;
```

### Object to Chuon String
#### C# and Java
``` C#
ChuonString chuonString = new ChuonString(objectdata);
```

### Object to Chuon Binary
#### C# and Java
``` C#
ChuonBinary chuonBinary = new ChuonBinary(objectdata);
```

### Chuon String to Object
#### C#
``` C#
Object objectdata = chuonString.ToObject();
```
#### Java
``` C#
Object objectdata = chuonString.toObject();
```

### Chuon Binary to Object
#### C#
``` C#
Object objectdata = chuonBinary.ToObject();
```
#### Java
``` C#
Object objectdata = chuonBinary.toObject();
```

### Chuon String to Chuon Binary
#### C#
``` C#
ChuonBinary chuonBinary = chuonString.ToChuonBinary();
```
#### Java
``` C#
ChuonBinary chuonBinary = chuonString.toChuonBinary();
```

### Chuon Binary to Chuon String
#### C#
``` C#
ChuonString chuonString = chuonBinary.ToChuonString();
```
#### Java
``` C#
ChuonString chuonString = chuonBinary.toChuonString();
```

### Chuon String Direct serialization
#### C#
``` C#
byte[] bytes = chuonString.ToBinaryArray(Encoding.<encoding>);
```
#### Java
``` C#
byte[] bytes = chuonString.toBinaryArray(Charset.forName("<encoding name>"));
```

### Serialize to Chuon String
#### C#
``` C#
ChuonString chuonString = new ChuonString(stringbytes, Encoding.<encoding>);
```
#### Java
``` C#
ChuonString chuonString = new ChuonString(stringbytes, Charset.forName("<encoding name>"));
```

## Example
### C#
[ChounTranslator](https://github.com/Jimmy01240397/Chuon/tree/master/ChuonCS/ChounTranslator)

![image](https://user-images.githubusercontent.com/57281249/141677492-09a5cecf-df4b-4acc-8574-dcef457d7efe.png)
