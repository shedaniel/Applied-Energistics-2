/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.sync;


import java.io.ByteArrayInputStream;

import org.apache.commons.lang3.tuple.Pair;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.NetworkHandler;


public abstract class AppEngPacket
{
	private PacketBuffer p;

	public void serverPacketData( final INetworkInfo manager, final PlayerEntity player )
	{
		throw new UnsupportedOperationException( "This packet ( " + this.getPacketID() + " does not implement a server side handler." );
	}

	public final int getPacketID()
	{
		return AppEngPacketHandlerBase.PacketTypes.getID( this.getClass() ).ordinal();
	}

	public void clientPacketData( final INetworkInfo network, final PlayerEntity player )
	{
		throw new UnsupportedOperationException( "This packet ( " + this.getPacketID() + " does not implement a client side handler." );
	}

	protected void configureWrite( final ByteBuf data )
	{
		data.capacity( data.readableBytes() );
		this.p = new PacketBuffer( data );
	}

	public IPacket<?> toPacket( NetworkDirection direction )
	{
		if( this.p.array().length > 2 * 1024 * 1024 ) // 2k walking room :)
		{
			throw new IllegalArgumentException( "Sorry AE2 made a " + this.p.array().length + " byte packet by accident!" );
		}

		if( AEConfig.instance().isFeatureEnabled( AEFeature.PACKET_LOGGING ) )
		{
			AELog.info( this.getClass().getName() + " : " + p.readableBytes() );
		}

		return direction.buildPacket( Pair.of( p, 0 ), NetworkHandler.instance().getChannel() ).getThis();
	}

	// TODO: Figure out why Forge/Minecraft on the server sets the stream data buffer to PooledUnsafeDirectByteBuf

	public ByteArrayInputStream getPacketByteArray( ByteBuf stream, int readerIndex, int readableBytes )
	{
		final ByteArrayInputStream bytes;
		if( stream.hasArray() )
		{
			bytes = new ByteArrayInputStream( stream.array(), readerIndex, readableBytes );
		}
		else
		{
			byte[] data = new byte[stream.capacity()];
			stream.getBytes( readerIndex, data, 0, readableBytes );
			bytes = new ByteArrayInputStream( data );
		}
		return bytes;
	}

	public ByteArrayInputStream getPacketByteArray( ByteBuf stream )
	{
		return this.getPacketByteArray( stream, 0, stream.readableBytes() );
	}
}
