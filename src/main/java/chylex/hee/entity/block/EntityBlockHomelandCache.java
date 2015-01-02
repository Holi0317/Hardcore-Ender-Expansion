package chylex.hee.entity.block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityBlockHomelandCache extends Entity{
	public EntityBlockHomelandCache(World world){
		super(world);
		setSize(1F,1F);
		preventEntitySpawning = true;
	}

	@Override
	protected void entityInit(){}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt){}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt){}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount){
		if (isEntityInvulnerable())return false;
		
		if (!isDead){
			setDead();
			
			if (worldObj.isRemote){
				worldObj.playSound(posX,posY,posZ,"dig.glass",1F,rand.nextFloat()*0.1F+0.92F,false);
				for(int a = 0; a < 20; a++)worldObj.spawnParticle("largesmoke",posX+(rand.nextDouble()-0.5D)*1.5D,posY+(rand.nextDouble()-0.5D)*1.5D,posZ+(rand.nextDouble()-0.5D)*1.5D,0D,0D,0D);
			}
			else{
				// TODO drops
			}
		}
		
		return true;
	}
	
	@Override
	protected boolean canTriggerWalking(){
		return false;
	}
	
	@Override
	public boolean canBeCollidedWith(){
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public float getShadowSize(){
		return 0F;
	}
}