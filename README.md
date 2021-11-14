# Chuon
Chummy Object Notation is a data interchange format and a serialization format that uses human-readable text to store and transmit data objects consisting of attribute–value pairs and arrays (or other serializable values). Or it can use the most suitable serialization format to serialize data objects consisting of attribute–value pairs and arrays (or other serializable values).

## Binary Format

| type | length(if it is Array) | data |
| -------- | -------- | -------- |
| 1 byte     | num is Base 128       | data binary length |

### Base 128
If first byte is Less than 128 than it don't have next digit.
If first byte is Greater than 128 than the number is:$(byte[0]-128) + (byte[1]-128) * 128^1 + ... + (byte[n - 1]-128) * 128^{n - 1} + byte[n] * 128^{n}$

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
| string     | 1 byte     |
| bool     | 1 byte     |
| object     | data_type_length + length(if it is Array) + data_length    |
| Dictionary     | key_type_length + data_type_length + length + (key_type_length + length(if it is Array) + key_length + data_type_length + length(if it is Array) + data_length)...     |

## Usage
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


## Example
### C#
[ChounTranslator](https://github.com/Jimmy01240397/Chuon/tree/master/ChuonCS/ChounTranslator)

![image](https://user-images.githubusercontent.com/57281249/141677492-09a5cecf-df4b-4acc-8574-dcef457d7efe.png)
