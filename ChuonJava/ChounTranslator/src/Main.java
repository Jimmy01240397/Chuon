import java.io.*;
import java.util.Scanner;

import chuon.*;


public class Main {
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Scanner in = new Scanner(System.in);
		PrintStream out = System.out;
		
		while (true) {
			out.println("1. byte to data\r\n2. data to byte");
			int choose = 0;
			for(choose = in.nextInt(); choose != 1 && choose != 2; choose = in.nextInt()) {}
			in.nextLine();
			switch (choose) {
			case 1:
			{
				out.println("Hex:");
				String data = in.nextLine();
                Object x = new ChuonBinary(StringTool.HexToBytes(data)).toObject();
                String aaa = new ChuonString(new ChuonBinary(StringTool.HexToBytes(data)).toObject()).toString();
                ChuonBinary vs = new ChuonBinary(StringTool.HexToBytes(data));
                out.println(vs.toChuonString().toStringWithEnter());

				break;
			}
			case 2:
			{
				out.println("ChuonString(end with \":::\"):");
				String data = "";
				while (true) {
					String now = in.nextLine();
					if (now.equals(":::"))
						break;
					
					data += now + "\r\n";
				}
				//out.println(data);
				//open(data);
                String aaa = StringTool.BytesToHex(new ChuonBinary(new ChuonString(data).toObject()).toArray());
				out.println(StringTool.BytesToHex(new ChuonString(data).toChuonBinary().toArray()));
				break;
			}
			default:
				break;
			}
		}
    }
	
	static void open(String text)
	{
		System.out.println("///");
		if (text.contains("{"))
		{
            String[] data = StringTool.TakeString(text, '{', '}');
			for (String string : data) {
				String nowString = string;
				System.out.println(nowString);
				open(nowString);
			}
		}
	}
}
