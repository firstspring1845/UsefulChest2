package nk0t.mods.usefulchest

import java.util

import net.minecraft.inventory.IInventory
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable.HashMap

object UsefulChestUtilities {

    class Stacks(var stack: Int = 0, val tagMap: HashMap[NBTTagCompound, Int] = new HashMap)

    class ItemStacks(val item: Item, val damageMap: HashMap[Int, Stacks] = new HashMap) {
        def getItemStacks = {
            damageMap.toArray.sortBy(_._1).flatMap(d => {
                (List((null, d._2.stack)) ++ d._2.tagMap).flatMap(s => {
                    val is = new ItemStack(item, 0, d._1)
                    is.setTagCompound(s._1)
                    val max = is.getMaxStackSize
                    (0 until s._2 by max).map(i => {
                        is.stackSize = Math.min(max, s._2 - i)
                        is.copy
                    })
                })
            })
        }
    }

    def sortUsefulChest(usefulchest : TileEntityUsefulChest) = {
        val m = new HashMap[Int, ItemStacks]
        usefulchest.usefulChestContents.foreach(is => {
                if(is != null) {
                    val s = m.getOrElseUpdate(Item.itemRegistry.getIDForObject(is.getItem), {new ItemStacks(is.getItem)}).damageMap.getOrElseUpdate(is.getItemDamage, {new Stacks})
                    is.getTagCompound match {
                        case nbt: NBTTagCompound => s.tagMap.put(nbt, s.tagMap.getOrElse(nbt, 0) + is.stackSize)
                        case _ => s.stack += is.stackSize
                    }
            }
        })
        usefulchest.usefulChestContents = new Array[ItemStack](usefulchest.getSizeInventory())
        m.toArray.sortBy(_._1).flatMap(_._2.getItemStacks).copyToArray(usefulchest.usefulChestContents)
    }
}
