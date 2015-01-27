package nk0t.mods.usefulchest.network

import cpw.mods.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import io.netty.buffer.ByteBuf
import net.minecraft.world.ChunkPosition
import nk0t.mods.usefulchest.{TileEntityUsefulChest, UsefulChest}

class MessageUsefulChest(var pos: ChunkPosition, var mode: Int, var page: Int) extends IMessage with IMessageHandler[MessageUsefulChest, IMessage] {

  def this() = this(null, 0, 0)

  override def fromBytes(buf: ByteBuf): Unit = {
    pos = new ChunkPosition(buf.readInt, buf.readInt, buf.readInt)
    mode = buf.readInt
    page = buf.readInt
  }

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(pos.chunkPosX)
    buf.writeInt(pos.chunkPosY)
    buf.writeInt(pos.chunkPosZ)
    buf.writeInt(mode)
    buf.writeInt(page)
  }

  override def onMessage(message: MessageUsefulChest, ctx: MessageContext): IMessage = {
    val player = ctx.getServerHandler.playerEntity
    player.getEntityWorld.getTileEntity(message.pos.chunkPosX, message.pos.chunkPosY, message.pos.chunkPosZ) match {
      case usefulChest: TileEntityUsefulChest => {
        message.mode match {
          case 0 => usefulChest.Page = message.page
          case 1 => usefulChest.sortChest
        }
        ctx.getServerHandler.playerEntity.openGui(UsefulChest.Instance, 0, player.getEntityWorld, usefulChest.xCoord, usefulChest.yCoord, usefulChest.zCoord)
      }
      case _ =>
    }
    null
  }
}
