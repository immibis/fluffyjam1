package com.immibis.fluffyjam1;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.network.packet.Packet250CustomPayload;

/**
 * Oversize 250 packets must be split up.
 * Instances of this class reassemble them.
 */
public class PacketAssembler {
	private ByteArrayOutputStream concatter = new ByteArrayOutputStream();
	
	/**
	 * Call this when a packet is received.
	 * If this forms a complete packet, the data will be returned.
	 */
	public byte[] feed(byte[] input) {
		if(input[0] == 0) {
			// non-segmented packet
			byte[] rv = new byte[input.length - 1];
			System.arraycopy(input, 1, rv, 0, rv.length);
			return rv;
		}
		
		if(input[0] == 1) {
			concatter.write(input, 1, input.length-1);
			return null;
		}
		
		if(input[0] == 2) {
			concatter.write(input, 1, input.length-1);
			byte[] rv = concatter.toByteArray();
			concatter.reset();
			return rv;
		}
		
		throw new RuntimeException("invalid split-packet type "+input[0]);
	}
	
	private static byte[] makeSubPacket(int type, byte[] dataFrom, int firstIndex, int lastIndex) {
		if(firstIndex >= dataFrom.length) return new byte[] {(byte)type};
		if(lastIndex > dataFrom.length) lastIndex = dataFrom.length;
		byte[] b = new byte[lastIndex - firstIndex + 1];
		if(b.length > 32767) throw new RuntimeException("packet too big, "+b.length);
		b[0] = (byte)type;
		System.arraycopy(dataFrom, firstIndex, b, 1, lastIndex - firstIndex);
		return b;
	}
	
	public static List<Packet250CustomPayload> splitPacket(String channel, byte[] data) {
		if(data.length < 32700) {
			return Arrays.asList(new Packet250CustomPayload(channel, makeSubPacket(0, data, 0, data.length)));
		}
		
		List<Packet250CustomPayload> rv = new ArrayList<Packet250CustomPayload>();
		for(int k = 0; k < data.length; k += 32700)
			rv.add(new Packet250CustomPayload(channel, makeSubPacket(1, data, k, k+32700)));
		
		rv.get(rv.size() - 1).data[0] = (byte)2;
		return rv;
	}
}
