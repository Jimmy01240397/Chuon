# Chuon
Chummy Object Notation is a data interchange format and a serialization format that uses human-readable text to store and transmit data objects consisting of attribute-value pairs and arrays (or other serializable values). 
Or it can use the most suitable serialization format to serialize data objects consisting of attribute-value pairs and arrays (or other serializable values).

* [Binary Format](#binary-format)
  * [length](#length)
    * [Base 128](#base-128)
  * [data binary](#data-binary)
    * [base type](#base-type)
    * [array](#array)
* [String Format](#string-format)
  * [byte](#byte)
  * [short](#short)
  * [int](#int)
  * [long](#long)
  * [ushort](#ushort)
  * [uint](#uint)
  * [ulong](#ulong)
  * [float](#float)
  * [double](#double)
  * [char](#char)
  * [string](#string)
  * [bool](#bool)
  * [object](#object)
  * [dict](#dict)
* [Usage](#usage)
* [Example](#example)

## Binary Format

| type | Array dimension | data |
| -------- | -------- | -------- |
| 1 byte | 1 byte | data binary |


### length
We use [Base 128](#base-128) to record all length contain Array length dict length....

#### Base 128
If first byte is Less than 128 than it don't have next digit.

If first byte is Greater than 128 than the number is:

(byte[0]-128) + (byte[1]-128) * 128^1 + ... + (byte[n - 1]-128) * 128^{n - 1} + byte[n] * 128^n

![](https://render.githubusercontent.com/render/math?math=(byte%5B0%5D-128)%20%2B%20(byte%5B1%5D-128)%20*%20128%5E1%20%2B%20...%20%2B%20(byte%5Bn%20-%201%5D-128)%20*%20128%5E%7Bn%20-%201%7D%20%2B%20byte%5Bn%5D%20*%20128%5En)

### data binary


#### base type
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
| char     | 1 byte     |
| string     | [string_length_length](#base-128) + string_length bytes     |
| bool     | 1 byte     |
| object     | 1 byte (for data type length) + 1 byte (for data type Array dimension) + [data binary](#data-binary) (if data type length is 00 and data Array dimension is 00 that mean this data is null) |
| dict     | 1 byte (for key type length) + 1 byte (for key type Array dimension) + 1 byte (for data type length) + 1 byte (for data type Array dimension) + [dict_length](#base-128) + ([key_binary](#data-binary) + [data_binary](#data-binary)) * dict_length |

#### array

| length | data null tag(data allow null element except "object") | datas |
| -------- | -------- | -------- |
| [Base 128](#base-128) | (length - 1) / 8 + 1 | ([data_binary](#data-binary)) * length |


## String Format
There are the types that you can use.
* [byte](#byte) 
* [short](#short) 
* [int](#int) 
* [long](#long) 
* [ushort](#ushort) 
* [uint](#uint) 
* [ulong](#ulong) 
* [float](#float) 
* [double](#double) 
* [char](#char) 
* [string](#string) 
* [bool](#bool) 
* [object](#object) 
* [dict](#dict) 

### byte
``` C#
byte:<number>
```

#### Array
``` C#
byte[]:
{
	<Hex(don't add 0x)>
}
```

``` C#
byte[][]:
{
	{
		1122ddff
	},
	{
		<Hex(don't add 0x)>
	}
}
```
### short
``` C#
short:<number>
```
#### Array
``` C#
short[]:
{
	10000,
	-20000
}
```
### int
``` C#
int:<number>
```
#### Array
``` C#
int[]:
{
	100000000,
	-200000000
}
```
### long
``` C#
long:<number>
```
#### Array
``` C#
long[]:
{
	1000000000000000000,
	-200000000000000000
}
```
### ushort
``` C#
ushort:<number>
```
#### Array
``` C#
ushort[]:
{
	10000,
	20000
}
```
### uint
``` C#
uint:<number>
```
#### Array
``` C#
uint[]:
{
	100000000,
	200000000
}
```
### ulong
``` C#
ulong:<number>
```
#### Array
``` C#
ulong[]:
{
	1000000000000000000,
	2000000000000000000
}
```
### float
``` C#
float:<floating-point number>
```
#### Array
``` C#
float[]:
{
	5.5555,
	-3.14
}
```
### double
``` C#
double:<floating-point number>
```
#### Array
``` C#
double[]:
{
	5.55555555555,
	-3.1415926
}
```
### char
``` C#
char:'<character>'
```
#### Array
``` C#
char[]:
{
	'a',
	'n'
}
```
### string
``` C#
string:"<string>"
```
#### Array
``` C#
string[]:
{
	"Hello World",
	"coming soon"
}
```
### bool
``` C#
bool:<boolean>
```
#### Array
``` C#
bool[]:
{
	011001
}
```
``` C#
bool[][]:
{
	{
		011001
	},
	{
		<many 0 or 1>
	}
}
```
### object
``` C#
object:null

```

#### Array
``` C#
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
``` C#
Dictionary:
{
    <key type>:<data type>:
    {
        <key>,
        <data>
    }
    {
        "now",
        111
    }
}
```
#### Array
``` C#
Dictionary[]:
{
	{
		int:string:
		{
			1,
			"bad"
		}
		{
			10,
			"good"
		}
	}
	{
		<key type>:<data type>:
		{
			<key>,
			<data>
		}
		{
			"now",
			111
		}
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
import chuon.ChuonString;
import chuon.ChuonBinary;
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

### Java
[ChounTranslator](https://github.com/Jimmy01240397/Chuon/tree/master/ChuonJava/ChounTranslator)
