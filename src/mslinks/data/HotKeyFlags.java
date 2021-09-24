/*
	https://github.com/BlackOverlord666/mslinks
	
	Copyright (c) 2015 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package mslinks.data;

import java.io.IOException;
import java.util.HashMap;

import io.ByteReader;
import io.ByteWriter;
import mslinks.Serializable;

public class HotKeyFlags implements Serializable {
  
  private static HashMap<Byte, String> keys = createMap();
  private static HashMap<Byte, String> createMap() {
    HashMap<Byte, String> result = new HashMap<>();
    result.put((byte)0x30, "0");
    result.put((byte)0x31, "1");
    result.put((byte)0x32, "2");
    result.put((byte)0x33, "3");
    result.put((byte)0x34, "4");
    result.put((byte)0x35, "5");
    result.put((byte)0x36, "6");
    result.put((byte)0x37, "7");
    result.put((byte)0x38, "8");
    result.put((byte)0x39, "9");
    result.put((byte)0x41, "A");
    result.put((byte)0x42, "B");
    result.put((byte)0x43, "C");
    result.put((byte)0x44, "D");
    result.put((byte)0x45, "E");
    result.put((byte)0x46, "F");
    result.put((byte)0x47, "G");
    result.put((byte)0x48, "H");
    result.put((byte)0x49, "I");
    result.put((byte)0x4A, "J");
    result.put((byte)0x4B, "K");
    result.put((byte)0x4C, "L");
    result.put((byte)0x4D, "M");
    result.put((byte)0x4E, "N");
    result.put((byte)0x4F, "O");
    result.put((byte)0x50, "P");
    result.put((byte)0x51, "Q");
    result.put((byte)0x52, "R");
    result.put((byte)0x53, "S");
    result.put((byte)0x54, "T");
    result.put((byte)0x55, "U");
    result.put((byte)0x56, "V");
    result.put((byte)0x57, "W");
    result.put((byte)0x58, "X");
    result.put((byte)0x59, "Y");
    result.put((byte)0x5A, "Z");
    result.put((byte)0x70, "F1");
    result.put((byte)0x71, "F2");
    result.put((byte)0x72, "F3");
    result.put((byte)0x73, "F4");
    result.put((byte)0x74, "F5");
    result.put((byte)0x75, "F6");
    result.put((byte)0x76, "F7");
    result.put((byte)0x77, "F8");
    result.put((byte)0x78, "F9");
    result.put((byte)0x79, "F10");
    result.put((byte)0x7A, "F11");
    result.put((byte)0x7B, "F12");
    result.put((byte)0x7C, "F13");
    result.put((byte)0x7D, "F14");
    result.put((byte)0x7E, "F15");
    result.put((byte)0x7F, "F16");
    result.put((byte)0x80, "F17");
    result.put((byte)0x81, "F18");
    result.put((byte)0x82, "F19");
    result.put((byte)0x83, "F20");
    result.put((byte)0x84, "F21");
    result.put((byte)0x85, "F22");
    result.put((byte)0x86, "F23");
    result.put((byte)0x87, "F24");
    result.put((byte)0x90, "NUM LOCK");
    result.put((byte)0x91, "SCROLL LOCK");
    result.put((byte)0x01, "SHIFT");
    result.put((byte)0x02, "CTRL");
    result.put((byte)0x04, "ALT");
    return result;
  }
	
	private static HashMap<String, Byte> keysr = new HashMap<>();
	
	static {
		for (java.util.Map.Entry<Byte,String> i : keys.entrySet())
			keysr.put(i.getValue(), i.getKey());
	}
	
	private byte low;
	private byte high;
	
	public HotKeyFlags() {
		low = high = 0;
	}
	
	public HotKeyFlags(ByteReader data) throws IOException {
		low = (byte)data.read();
		high = (byte)data.read();
	}
	
	public String getKey() {
		return keys.get(low);
	}
	
	public HotKeyFlags setKey(String k) {
		if (k != null && !k.equals(""))
			low = keysr.get(k);
		return this;
	}
	
	public boolean isShift() { return (high & 1) != 0; }
	public boolean isCtrl() { return (high & 2) != 0; }
	public boolean isAlt() { return (high & 4) != 0; }
	
	public HotKeyFlags setShift() { high = (byte)(1 | (high & 6)); return this; }
	public HotKeyFlags setCtrl() { high = (byte)(2 | (high & 5)); return this; }
	public HotKeyFlags setAlt() { high = (byte)(4 | (high & 3)); return this; }
	
	public HotKeyFlags clearShift() { high = (byte)(high & 6); return this; }
	public HotKeyFlags clearCtrl() { high = (byte)(high & 5); return this; }
	public HotKeyFlags clearAlt() { high = (byte)(high & 3); return this; }

	public void serialize(ByteWriter bw) throws IOException {
		bw.write(low);
		bw.write(high);
	}
}
