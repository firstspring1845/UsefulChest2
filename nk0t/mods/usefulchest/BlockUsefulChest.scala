package nk0t.mods.usefulchest

import net.minecraft.block.{Block, BlockContainer}
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MathHelper
import net.minecraft.world.World

import scala.util.Random

class BlockUsefulChest extends BlockContainer(Material.wood) {

    val random = new Random

    this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F)
    this.setHardness(5.0f)
    this.setResistance(10.0f)
    this.setStepSound(Block.soundTypeMetal)

    override def createNewTileEntity(world: World, meta: Int): TileEntity = {
        return new TileEntityUsefulChest;
    }

    override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int,
                                 entityLivingBase: EntityLivingBase, itemstack: ItemStack) = {

        var tileentity = world.getTileEntity(x, y, z).asInstanceOf[TileEntityUsefulChest]
        var direction = MathHelper.floor_double(((entityLivingBase.rotationYaw * 4F) / 360F).toDouble + 0.5D) & 3
        tileentity.Direction = direction.toByte
    }

    override def isOpaqueCube = false

    override def renderAsNormalBlock = false

    override def getRenderType = UsefulChest.usefulChestRendererId

    override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer,
                                  s: Int, p: Float, q: Float, r: Float): Boolean = {

        if (world.isRemote) {
            return true
        }
        else {
            player.openGui(UsefulChest.Instance, 0, world, x, y, z)
            return true
        }
    }

    override def breakBlock(world: World, i: Int, j: Int, k: Int, block: Block, m: Int) = {

        val tileentitychest = world.getTileEntity(i, j, k).asInstanceOf[TileEntityUsefulChest]

        if (tileentitychest != null) {
            for (j1 <- 0 to tileentitychest.getSizeInventory - 1) {
                val itemstack = tileentitychest.getStackInSlot(j1)

                if (itemstack != null) {
                    val f = this.random.nextFloat() * 0.8F + 0.1F
                    val f1 = this.random.nextFloat() * 0.8F + 0.1F
                    val f2 = this.random.nextFloat() * 0.8F + 0.1F
                    var entityitem: EntityItem = null

                    while (itemstack.stackSize > 0) {

                        var k1 = this.random.nextInt(21) + 10

                        if (k1 > itemstack.stackSize) {
                            k1 = itemstack.stackSize;
                        }

                        itemstack.stackSize -= k1;

                        entityitem = new EntityItem(world, (i.toFloat + f).toDouble,
                            (j.toFloat + f1).toDouble,
                            (k.toFloat + f2).toDouble,
                            new ItemStack(itemstack.getItem, k1, itemstack.getItemDamage))
                        val f3 = 0.05F;

                        entityitem.motionX = (this.random.nextGaussian().toFloat * f3).toDouble;
                        entityitem.motionY = (this.random.nextGaussian().toFloat * f3 + 0.2f).toDouble;
                        entityitem.motionZ = (this.random.nextGaussian().toFloat * f3).toDouble;

                        if (itemstack.hasTagCompound) {

                            entityitem.getEntityItem().setTagCompound(itemstack.getTagCompound.copy.asInstanceOf[NBTTagCompound]);
                        }

                        world.spawnEntityInWorld(entityitem)
                    }
                }
            }

            world.func_147453_f(i, j, k, block);
        }

        super.breakBlock(world, i, j, k, block, m)
    }

    override def getIcon(side: Int, meta: Int) = Blocks.diamond_block.getIcon(0, 0)
}