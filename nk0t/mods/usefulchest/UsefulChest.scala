package nk0t.mods.usefulchest

import cpw.mods.fml.client.registry.RenderingRegistry
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.registry.LanguageRegistry
import cpw.mods.fml.relauncher.Side
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.{Items, Blocks}
import net.minecraft.item.ItemStack
import net.minecraftforge.common.config.{ConfigCategory, Configuration}
import nk0t.mods.usefulchest.common.CommonProxy
import nk0t.mods.usefulchest.network.{MessageUsefulChest, GuiHandler}

@Mod(modid = "UsefulChest2", name = "UsefulChest2", version = "1.0.0", modLanguage = "scala")
class UsefulChest {

    var debug = false

    @EventHandler
    def preInit(event : FMLPreInitializationEvent) {
        val conf = new Configuration(event.getSuggestedConfigurationFile)
        conf.load
        debug = conf.get(Configuration.CATEGORY_GENERAL, "debug", false).getBoolean
        UsefulChest.Instance = this
    }

    @EventHandler
    def init(event : FMLInitializationEvent) = {
        UsefulChest.packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel("UsefulChest2")
        UsefulChest.packetHandler.registerMessage(classOf[MessageUsefulChest], classOf[MessageUsefulChest], 0, Side.SERVER)

        UsefulChest.usefulChestBlock = new BlockUsefulChest
        UsefulChest.usefulChestBlock.setBlockName("UsefulChest")
        UsefulChest.usefulChestBlock.setCreativeTab(CreativeTabs.tabDecorations)

        GameRegistry.registerBlock(UsefulChest.usefulChestBlock, "UsefulChest")
        LanguageRegistry.addName(UsefulChest.usefulChestBlock, "Useful Chest")
        LanguageRegistry.instance().addNameForObject(UsefulChest.usefulChestBlock, "ja_JP", "ユースフルチェスト")

        GameRegistry.addShapedRecipe(new ItemStack(UsefulChest.usefulChestBlock)
        ,"idi"
        ,"dcd"
        ,"idi"
        ,Character.valueOf('i')
        ,Items.iron_ingot
        ,Character.valueOf('d')
        ,Items.diamond
        ,Character.valueOf('c')
        ,Blocks.chest)
        if(debug) GameRegistry.addShapelessRecipe(new ItemStack(UsefulChest.usefulChestBlock, 64), Blocks.dirt)

        UsefulChest.proxy.registerRenderers()
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler())
    }

}

object UsefulChest extends UsefulChest {

    var Instance : UsefulChest = null

    @SidedProxy(
        clientSide = "nk0t.mods.usefulchest.client.ClientProxy",
        serverSide = "nk0t.mods.usefulchest.common.CommonProxy")
    var proxy : CommonProxy = null

    val usefulChestRendererId = RenderingRegistry.getNextAvailableRenderId()

    var usefulChestBlock : Block = null

    var packetHandler : SimpleNetworkWrapper = null

}
