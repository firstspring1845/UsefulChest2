package nk0t.mods.usefulchest.network

import cpw.mods.fml.common.network.IGuiHandler
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import nk0t.mods.usefulchest.ContainerUsefulChest
import nk0t.mods.usefulchest.TileEntityUsefulChest
import nk0t.mods.usefulchest.client.GuiUsefulChest

class GuiHandler extends IGuiHandler {

    override def getServerGuiElement(ID : Int, player : EntityPlayer,
                                     world : World, x : Int, y : Int, z : Int) : Object = {
        val tileEntity = world.getTileEntity(x, y, z)

        tileEntity match {
            case usefulChest : TileEntityUsefulChest => new ContainerUsefulChest(player.inventory, usefulChest, usefulChest.Page)
            case _ => null
        }
    }

    override def getClientGuiElement(ID : Int, player : EntityPlayer,
                                     world : World, x : Int, y : Int, z : Int) : Object = {
        val tileEntity = world.getTileEntity(x, y, z)
        tileEntity match {
            case usefulChest : TileEntityUsefulChest => new GuiUsefulChest(player.inventory, usefulChest, usefulChest.Page, world, x, y, z)
            case _ => null
        }
    }

}