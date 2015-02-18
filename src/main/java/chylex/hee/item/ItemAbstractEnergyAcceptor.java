package chylex.hee.item;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import chylex.hee.HardcoreEnderExpansion;
import chylex.hee.block.BlockList;
import chylex.hee.mechanics.energy.EnergyChunkData;
import chylex.hee.system.savedata.WorldDataHandler;
import chylex.hee.system.savedata.types.EnergySavefile;
import chylex.hee.system.util.ItemUtil;
import chylex.hee.system.util.MathUtil;
import chylex.hee.tileentity.TileEntityEnergyCluster;

public abstract class ItemAbstractEnergyAcceptor extends Item{
	public abstract boolean canAcceptEnergy(ItemStack is);
	public abstract void onEnergyAccepted(ItemStack is);
	public abstract int getEnergyPerUse(ItemStack is);
	protected abstract float getRegenSpeedMultiplier();
	
	@Override
	public void onUpdate(ItemStack is, World world, Entity entity, int slot, boolean isHeld){
		if (!canAcceptEnergy(is))return;
		
		NBTTagCompound nbt = ItemUtil.getNBT(is,true);

		if (nbt.hasKey("engDrain") && entity instanceof EntityPlayer){
			boolean stop = false;
			int[] loc = nbt.getIntArray("engDrain");
			byte wait = nbt.getByte("engWait");
			
			if (!world.isRemote && Math.abs(nbt.getFloat("engDist")-MathUtil.distance(loc[0]+0.5D-entity.posX,loc[1]+0.5D-entity.posY,loc[2]+0.5D-entity.posZ)) > 0.05D)stop = true;
			else if (wait > 0)nbt.setByte("engWait",(byte)(wait-1));
			else{
				TileEntity tile = world.getTileEntity(new BlockPos(loc[0],loc[1],loc[2]));
				
				if (tile instanceof TileEntityEnergyCluster){
					TileEntityEnergyCluster cluster = (TileEntityEnergyCluster)tile;
					
					if (cluster.data.drainEnergyUnit()){
						cluster.synchronize();
						
						if (!world.isRemote)onEnergyAccepted(is);
						else{
							Random rand = world.rand;
							BlockPos pos = cluster.getPos();
							
							for(int a = 0; a < 26; a++){
								HardcoreEnderExpansion.fx.energyClusterMoving(world,pos.getX()+0.5D+(rand.nextFloat()-0.5D)*0.2D,pos.getY()+0.5D+(rand.nextFloat()-0.5D)*0.2D,pos.getZ()+0.5D+(rand.nextFloat()-0.5D)*0.2D,(rand.nextFloat()-0.5D)*0.4D,(rand.nextFloat()-0.5D)*0.4D,(rand.nextFloat()-0.5D)*0.4D,cluster.getColor(0),cluster.getColor(1),cluster.getColor(2));
							}
						}
					}
					else stop = true;
				}
				else stop = true;
				
				if (!stop)nbt.setByte("engWait",(byte)4);
			}
			
			if (stop){
				nbt.removeTag("engDrain");
				nbt.removeTag("engWait");
				nbt.removeTag("engDist");
			}
		}
		
		if (world.provider.getDimensionId() == 1){
			short timer = nbt.getShort("engRgnTim");
			
			if (++timer <= (42+world.rand.nextInt(20))/getRegenSpeedMultiplier()){
				nbt.setShort("engRgnTim",timer);
				return;
			}
			else nbt.setShort("engRgnTim",(short)0);
			
			EnergyChunkData chunk = WorldDataHandler.<EnergySavefile>get(EnergySavefile.class).getFromBlockCoords(world,(int)entity.posX,(int)entity.posZ,true);
			if (chunk.drainEnergyUnit())onEnergyAccepted(is);
		}
	}
	
	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ){
		NBTTagCompound tag = ItemUtil.getNBT(is,true);
		
		if (world.getBlockState(pos).getBlock() == BlockList.energy_cluster && canAcceptEnergy(is)){
			if (tag.hasKey("engDrain")){
				tag.removeTag("engDrain");
				tag.removeTag("engWait");
				tag.removeTag("engDist");
			}
			else if (world.getTileEntity(pos) instanceof TileEntityEnergyCluster){
				tag.setIntArray("engDrain",new int[]{ pos.getX(), pos.getY(), pos.getZ() });
				tag.setFloat("engDist",(float)MathUtil.distance(pos.getX()+0.5D-player.posX,pos.getY()+0.5D-player.posY,pos.getZ()+0.5D-player.posZ));
			}
			
			return true;
		}
		
		return false;
	}
}
