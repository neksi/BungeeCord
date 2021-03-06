package net.md_5.bungee.netty.encoders;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.md_5.bungee.netty.PacketMapping;
import net.md_5.bungee.netty.Var;
import net.md_5.bungee.netty.decoders.PacketTranslatorDecoder;
import net.md_5.bungee.netty.packetrewriter.PacketRewriter;

@ChannelHandler.Sharable
public class PacketTranslatorEncoder extends MessageToByteEncoder<ByteBuf>
{

    PacketTranslatorDecoder trDecoder;

    public PacketTranslatorEncoder(PacketTranslatorDecoder trDecoder)
    {
        this.trDecoder = trDecoder;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception
    {
        if ( trDecoder.getNextState() == PacketTranslatorDecoder.INGAME )
        {
            short packetId = msg.readUnsignedByte();
            PacketRewriter rewriter = PacketMapping.rewriters[ packetId ];
            int mappedPacketId = PacketMapping.spm[ packetId ];
            Var.writeVarInt( mappedPacketId, out );
            if ( rewriter == null )
            {
                out.writeBytes( msg.readBytes( msg.readableBytes() ) );
            } else
            {
                rewriter.rewriteServerToClient( msg, out );
            }
        } else
        {
            short packetId = msg.readUnsignedByte();
            if ( packetId == 0x02 )
            {
                trDecoder.setNextState( 3 );
            }
            Var.writeVarInt( packetId, out );
            out.writeBytes( msg.readBytes( msg.readableBytes() ) );
        }
    }
}
