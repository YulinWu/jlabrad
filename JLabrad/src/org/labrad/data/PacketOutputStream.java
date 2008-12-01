package org.labrad.data;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.labrad.types.Type;

/**
 * Output stream that writes LabRAD packets.
 * @author maffoo
 *
 */
public class PacketOutputStream extends BufferedOutputStream {

    public PacketOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Writes a packet to the output stream.
     * @param packet
     * @throws IOException
     */
    public void writePacket(Packet packet) throws IOException {
        writePacket(packet.getContext(), packet.getTarget(),
        		    packet.getRequest(), packet.getRecords());
    }

    public void writePacket(Context context, long target, int request,
    		Record... records) throws IOException {
    	writePacket(context.getHigh(), context.getLow(), target, request, records);
    }
    
    public void writePacket(long high, long low, long target, int request,
            Record... records) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // flatten records
        for (Record rec : records) {
            Data recData = new Data(Type.RECORD_TYPE);
            recData.setWord(rec.getID(), 0);
            recData.setString(rec.getData().getTag(), 1);
            recData.setBytes(rec.getData().flatten(), 2);
            os.write(recData.flatten());
        }

        // flatten packet header and append records
        Data packetData = new Data(Type.PACKET_TYPE);
        packetData.setWord(high, 0);
        packetData.setWord(low, 1);
        packetData.setInt(request, 2);
        packetData.setWord(target, 3);
        packetData.setBytes(os.toByteArray(), 4);

        out.write(packetData.flatten());
        out.flush();
    }
}
