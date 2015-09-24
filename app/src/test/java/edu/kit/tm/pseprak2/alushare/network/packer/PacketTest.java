package edu.kit.tm.pseprak2.alushare.network.packer;

import org.junit.After;
import org.junit.Test;

import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Albrecht Weiche
 */
public class PacketTest {

    private byte[] data = ("aksdjfölkajsdöfkljasldökj82139745´ß1289357930485o4iu6opöi+ß09134cmp" +
            "krv,toretpjwgeriotjieomgvwbhriufhbnwioprugjthmovjgölkfdjgbökljfgklösdjfbgklöjsvpow" +
            "ieq+epropeqwüoüpqweor+üpqweor+üpqweor+üpqweorü+peqworöldskfgöläkdgslöäsdfkglödkglö" +
            "dsfkgölksdföglkäfölkasdjfklöajsödlkfjaöskldjföaklsdjfölkasdjfölkasjdföklajsdöflkja" +
            "krv,toretpjwgeriotjieomgvwbhriufhbnwioprugjthmovjgölkfdjgbökljfgklösdjfbgklöjsvpow" +
            "ieq+epropeqwüoüpqweor+üpqweor+üpqweor+üpqweorü+peqworöldskfgöläkdgslöäsdfkglödkglö" +
            "dsfkgölksdföglkäfölkasdjfklöajsödlkfjaöskldjföaklsdjfölkasdjfölkasjdföklajsdöflkja" +
            "krv,toretpjwgeriotjieomgvwbhriufhbnwioprugjthmovjgölkfdjgbökljfgklösdjfbgklöjsvpow" +
            "ieq+epropeqwüoüpqweor+üpqweor+üpqweor+üpqweorü+peqworöldskfgöläkdgslöäsdfkglödkglö" +
            "dsfkgölksdföglkäfölkasdjfklöajsödlkfjaöskldjföaklsdjfölkasdjfölkasjdföklajsdöflkja" +
            "krv,toretpjwgeriotjieomgvwbhriufhbnwioprugjthmovjgölkfdjgbökljfgklösdjfbgklöjsvpow" +
            "ieq+epropeqwüoüpqweor+üpqweor+üpqweor+üpqweorü+peqworöldskfgöläkdgslöäsdfkglödkglö" +
            "dsfkgölksdföglkäfölkasdjfklöajsödlkfjaöskldjföaklsdjfölkasdjfölkasjdföklajsdöflkja" +
            "krv,toretpjwgeriotjieomgvwbhriufhbnwioprugjthmovjgölkfdjgbökljfgklösdjfbgklöjsvpow" +
            "ieq+epropeqwüoüpqweor+üpqweor+üpqweor+üpqweorü+peqworöldskfgöläkdgslöäsdfkglödkglö" +
            "dsfkgölksdföglkäfölkasdjfklöajsödlkfjaöskldjföaklsdjfölkasdjfölkasjdföklajsdöflkja" +
            "krv,toretpjwgeriotjieomgvwbhriufhbnwioprugjthmovjgölkfdjgbökljfgklösdjfbgklöjsvpow" +
            "ieq+epropeqwüoüpqweor+üpqweor+üpqweor+üpqweorü+peqworöldskfgöläkdgslöäsdfkglödkglö" +
            "dsfkgölksdföglkäfölkasdjfklöajsödlkfjaöskldjföaklsdjfölkasdjfölkasjdföklajsdöflkja" +
            "krv,toretpjwgeriotjieomgvwbhriufhbnwioprugjthmovjgölkfdjgbökljfgklösdjfbgklöjsvpow" +
            "ieq+epropeqwüoüpqweor+üpqweor+üpqweor+üpqweorü+peqworöldskfgöläkdgslöäsdfkglödkglö" +
            "dsfkgölksdföglkäfölkasdjfklöajsödlkfjaöskldjföaklsdjfölkasdjfölkasjdföklajsdöflkja" +
            "krv,toretpjwgeriotjieomgvwbhriufhbnwioprugjthmovjgölkfdjgbökljfgklösdjfbgklöjsvpow" +
            "ieq+epropeqwüoüpqweor+üpqweor+üpqweor+üpqweorü+peqworöldskfgöläkdgslöäsdfkglödkglö" +
            "dsfkgölksdföglkäfölkasdjfklöajsödlkfjaöskldjföaklsdjfölkasdjfölkasjdföklajsdöflkja" +
            "krv,toretpjwgeriotjieomgvwbhriufhbnwioprugjthmovjgölkfdjgbökljfgklösdjfbgklöjsvpow" +
            "ieq+epropeqwüoüpqweor+üpqweor+üpqweor+üpqweorü+peqworöldskfgöläkdgslöäsdfkglödkglö" +
            "dsfkgölksdföglkäfölkasdjfklöajsödlkfjaöskldjföaklsdjfölkasdjfölkasjdföklajsdöflkja" +
            "sdölkfjioüewruqweüriuopqweiropqweiropüoäasödl,:_YXcv,:_XjklljkljkljklC,").getBytes();


    @Test
    public void testEncodeDecode() {
        Packet packet = new Packet(0, 1, 123904890l, data, "receiver.onion", "sender.onion");
        byte[] packetByte = Packet.packetToByte(packet);
        Packet rePacket = Packet.byteToPacket(packetByte, "receiver.onion", "sender.onion");
        byte[] reData = rePacket.data;
        assertArrayEquals("packet and unpackt data should be equal", data, reData);
        assertEquals("Sequence number should be equal!", packet.sequenceNumber, rePacket.sequenceNumber);
        assertEquals("Sequence count should be equal!", packet.packetCount, rePacket.packetCount);
        assertEquals("Data identifier should be equal!", packet.dataIdentifier, packet.dataIdentifier);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooBigPacket() {
        byte[] tooMuch = new byte[ProtocolConstants.PACKET_MAX_DATA_SIZE + 1];
        new Packet(0, 1, 123904890l, tooMuch, "receiver.onion", "sender.onion");
    }

    @After
    public void teardown() {
        TestHelper.resetHelperFactory();
    }
}
