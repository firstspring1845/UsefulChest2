package nk0t.mods.usefulchest

import net.minecraft.network.{NetworkManager, Packet}
import net.minecraft.network.play.server.S35PacketUpdateTileEntity

import scala.collection.JavaConversions.asScalaBuffer

import cpw.mods.fml.relauncher.Side
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.AxisAlignedBB

class TileEntityUsefulChest extends TileEntity with IInventory {

    var usefulChestContents = new Array[ItemStack](getSizeInventory)

    var Direction : Byte = 0
    var Page : Int = 1

    var lidAngle : Float = 0
    var prevLidAngle : Float = 0
    var numUsingPlayers : Int = 0
    var ticksSinceSync : Int = 0

    def sortChest = {
        UsefulChestUtilities.sortUsefulChest(this)
    }

    override def updateEntity = {

        super.updateEntity

        this.ticksSinceSync += 1
        var f = 0F

        if (!this.worldObj.isRemote && this.numUsingPlayers != 0 &&
            (this.ticksSinceSync + this.xCoord + this.yCoord + this.zCoord) % 200 == 0) {
            this.numUsingPlayers = 0
            f = 5.0F

            var d = (this.xCoord.toFloat - f).toDouble
            val aabb = AxisAlignedBB.getBoundingBox(
                (this.xCoord.toFloat - f).toDouble,
                (this.yCoord.toFloat - f).toDouble,
                (this.zCoord.toFloat - f).toDouble,
                (this.xCoord.toFloat + f).toDouble,
                (this.yCoord.toFloat + f).toDouble,
                (this.zCoord.toFloat + f).toDouble)
            var list = this.worldObj.getEntitiesWithinAABB(classOf[EntityPlayer], aabb)

            for (l <- list) {
                var entityPlayer = l.asInstanceOf[EntityPlayer]
                if (entityPlayer.openContainer.isInstanceOf[ContainerUsefulChest]) {
                    var iinventory = entityPlayer.openContainer.asInstanceOf[ContainerUsefulChest].getChestInventory
                    if (iinventory == this) {
                        this.numUsingPlayers += 1
                    }
                }
            }
        }

        this.prevLidAngle = this.lidAngle
        f = 0.1F
        var d0 = 0d

        if (this.numUsingPlayers > 0 && this.lidAngle == 0.0F) {
            var d1 = this.xCoord.toDouble + 0.5d
            d0 = this.zCoord.toDouble + 0.5d
            this.worldObj.playSoundEffect(d1, this.yCoord.toDouble + 0.5d, d0, "random.chestopen",
                0.5f, this.worldObj.rand.nextFloat * 0.1f + 0.9F)
        }

        if (this.numUsingPlayers == 0 && this.lidAngle > 0.0f || this.numUsingPlayers > 0 && this.lidAngle < 1.0f) {
            var f1 = this.lidAngle

            if (this.numUsingPlayers > 0) {
                this.lidAngle += f
            }
            else {
                this.lidAngle -= f
            }
            if (this.lidAngle > 1.0f) {
                this.lidAngle = 1.0f
            }

            var f2 = 0.5f
            if (this.lidAngle < f2 && f1 >= f2) {
                d0 = this.xCoord.toDouble + 0.05D
                var d2 = this.zCoord.toDouble + 0.05D

                this.worldObj.playSoundEffect(d0, this.yCoord.toDouble + 0.5d, d2, "random.chestclosed",
                    0.5f, this.worldObj.rand.nextFloat * 0.1f + 0.9F)
            }

            if (this.lidAngle < 0.0f) {
                this.lidAngle = 0.0f
            }
        }
    }

    override def readFromNBT(nbtTagCompound : NBTTagCompound) = {

        super.readFromNBT(nbtTagCompound)

        val nbtItems = nbtTagCompound.getTagList("Items", 10)
        this.usefulChestContents = new Array[ItemStack](getSizeInventory)

        for (i <- 0 to nbtItems.tagCount - 1) {
            val nbtItem = nbtItems.getCompoundTagAt(i)
            val slot = nbtItem.getInteger("Slot")
            if (slot >= 0 && slot < usefulChestContents.length) {
                usefulChestContents(slot) = ItemStack.loadItemStackFromNBT(nbtItem)
            }
        }

        this.Direction = nbtTagCompound.getByte(UsefulChestNBT.Direction)
    }

    override def writeToNBT(nbtTagCompound : NBTTagCompound) = {

        super.writeToNBT(nbtTagCompound)

        val nbtItems = new NBTTagList
        for (i <- 0 to this.usefulChestContents.length - 1) {
            if (usefulChestContents(i) != null) {
                val nbtItem = new NBTTagCompound
                nbtItem.setInteger("Slot", i)
                usefulChestContents(i).writeToNBT(nbtItem)
                nbtItems.appendTag(nbtItem)
            }
        }

        nbtTagCompound.setTag("Items", nbtItems)
        nbtTagCompound.setByte(UsefulChestNBT.Direction, this.Direction)
    }

    override def getDescriptionPacket : Packet = {
        val nbt = new NBTTagCompound
        nbt.setInteger(UsefulChestNBT.Page, this.Page)
        nbt.setByte(UsefulChestNBT.Direction, this.Direction)
        val nbt_ = new NBTTagCompound
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -1, nbt)
    }

    override def onDataPacket(net: NetworkManager, pkt: S35PacketUpdateTileEntity): Unit = {
        val nbt = pkt.func_148857_g
        this.Page = nbt.getInteger(UsefulChestNBT.Page)
        this.Direction = nbt.getByte(UsefulChestNBT.Direction)
    }

    override def getSizeInventory = 1040

    override def getStackInSlot(index : Int) = usefulChestContents(index)

    override def decrStackSize(index : Int, size : Int) : ItemStack = {

        if (usefulChestContents(index) != null) {
            var itemstack : ItemStack = null

            if (this.usefulChestContents(index).stackSize <= size) {
                itemstack = this.usefulChestContents(index)
                this.usefulChestContents(index) = null
                this.markDirty
                return itemstack
            }
            else {
                itemstack = this.usefulChestContents(index).splitStack(size)
                if (this.usefulChestContents(index).stackSize == 0) {
                    this.usefulChestContents(index) = null
                }
                this.markDirty
                return itemstack
            }
        }
        else {
            return null
        }
    }

    override def getStackInSlotOnClosing(index : Int) : ItemStack = {

        if (this.usefulChestContents(index) != null) {
            var itemstack = this.usefulChestContents(index)
            this.usefulChestContents(index) = null
            return itemstack
        }
        else {
            return null
        }
    }

    override def setInventorySlotContents(index : Int, itemstack : ItemStack) = {
        this.usefulChestContents(index) = itemstack

        if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit) {
            itemstack.stackSize = this.getInventoryStackLimit
        }

        this.markDirty
    }

    override def getInventoryName = "UsefulChest"

    override def hasCustomInventoryName = false

    override def getInventoryStackLimit = 64

    override def isUseableByPlayer(entityplayer : EntityPlayer) : Boolean = {

        if (this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this) {
            return false
        }
        return entityplayer.getDistanceSq(
            this.xCoord.toDouble + 0.5D,
            this.yCoord.toDouble + 0.5D,
            this.zCoord.toDouble + 0.5D) <= 64.0D
    }

    override def receiveClientEvent(i : Int, j : Int) : Boolean = {

        if (i == 1) {
            this.numUsingPlayers = j
            return true
        }
        else {
            return super.receiveClientEvent(i, j)
        }
    }

    override def openInventory = {

        if (this.numUsingPlayers < 0) {
            this.numUsingPlayers = 0
        }
        this.numUsingPlayers += 1

        this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord,
            this.getBlockType, 1, this.numUsingPlayers)
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType)
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType)
    }

    override def closeInventory = {

        if (this.getBlockType != null && this.getBlockType.isInstanceOf[BlockUsefulChest]) {
            this.numUsingPlayers -= 1
        }

        this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord,
            this.blockType, 1, this.numUsingPlayers)
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.blockType)
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType)
    }

    override def isItemValidForSlot(i : Int, itemstack : ItemStack) = true
}
