package chylex.hee.world.structure;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import chylex.hee.system.abstractions.BlockInfo;
import chylex.hee.system.abstractions.Pos.PosMutable;
import chylex.hee.system.util.MathUtil;
import com.google.common.base.Objects;

public final class StructureWorld{
	private final int radX, radZ, sizeX, sizeY, sizeZ;
	private final Block[] blocks;
	private final byte[] metadata;
	private final TIntHashSet scheduledUpdates = new TIntHashSet(32);
	private final TIntObjectHashMap<BlockInfo> attentionWhores = new TIntObjectHashMap<>(16);
	private final TIntObjectHashMap<IStructureTileEntity> tileEntityMap = new TIntObjectHashMap<>(32);
	private final List<Entity> entityList = new ArrayList<>(8);
	
	public StructureWorld(int radX, int sizeY, int radZ){
		this.radX = radX;
		this.radZ = radZ;
		this.sizeX = radX*2+1;
		this.sizeY = sizeY;
		this.sizeZ = radZ*2+1;
		this.blocks = new Block[sizeX*sizeY*sizeZ];
		this.metadata = new byte[sizeX*sizeY*sizeZ];
	}
	
	public boolean isInside(int x, int y, int z){
		x += radX;
		z += radZ;
		return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ;
	}
	
	private int toIndex(int x, int y, int z){
		return y+sizeY*(x+radX)+sizeY*sizeX*(z+radZ);
	}
	
	private void toPos(int index, PosMutable pos){
		pos.setZ(MathUtil.floor((double)index/(sizeY*sizeX)));
		pos.setX(MathUtil.floor((double)(index-(pos.getZ()*sizeY*sizeX))/sizeY));
		pos.setY(index-(sizeY*sizeX*pos.getZ())-(sizeY*pos.getX()));
		pos.x -= radX;
		pos.z -= radZ;
	}
	
	public boolean setBlock(int x, int y, int z, Block block){
		return setBlock(x,y,z,block,0);
	}
	
	public boolean setBlock(int x, int y, int z, Block block, int metadata){
		if (!isInside(x,y,z))return false;
		
		int index = toIndex(x,y,z);
		this.blocks[index] = block;
		this.metadata[index] = (byte)metadata;
		return true;
	}
	
	public boolean setBlock(int x, int y, int z, Block block, int metadata, boolean scheduleUpdate){
		if (setBlock(x,y,z,block,metadata)){
			if (scheduleUpdate)this.scheduledUpdates.add(toIndex(x,y,z));
			return true;
		}
		else return false;
	}
	
	public boolean setBlock(int x, int y, int z, BlockInfo blockInfo){
		return setBlock(x,y,z,blockInfo.block,blockInfo.meta);
	}
	
	public boolean setAir(int x, int y, int z){
		return setBlock(x,y,z,Blocks.air,0);
	}
	
	public void setAttentionWhore(int x, int y, int z, BlockInfo info){
		attentionWhores.put(toIndex(x,y,z),info);
	}
	
	public boolean setTileEntity(int x, int y, int z, IStructureTileEntity provider){
		if (!isInside(x,y,z))return false;
		
		tileEntityMap.put(toIndex(x,y,z),provider);
		return true;
	}
	
	public Block getBlock(int x, int y, int z){
		return isInside(x,y,z) ? Objects.firstNonNull(this.blocks[toIndex(x,y,z)],Blocks.air) : Blocks.air;
	}
	
	public int getMetadata(int x, int y, int z){
		return isInside(x,y,z) ? this.metadata[toIndex(x,y,z)] : 0;
	}
	
	public boolean isAir(int x, int y, int z){
		return !isInside(x,y,z) || Objects.firstNonNull(this.blocks[toIndex(x,y,z)],Blocks.air) == Blocks.air;
	}
	
	public void addEntity(Entity entity){
		entityList.add(entity);
	}
	
	public <T extends Entity> Stream<T> getEntities(final Class<T> exactClassToMatch){
		return (Stream<T>)entityList.stream().filter(entity -> entity.getClass() == exactClassToMatch);
	}
	
	public void generateInWorld(World world, Random rand, int centerX, int bottomY, int centerZ){
		PosMutable pos = new PosMutable();
		int x, y, z, index = -1;

		for(z = -radZ; z <= radZ; z++){
			for(x = -radX; x <= radX; x++){
				for(y = 0; y < sizeY; y++){
					if (blocks[++index] != null)pos.set(centerX+x,bottomY+y,centerZ+z).setBlock(world,blocks[index],metadata[index],2);
				}
			}
		}
		
		attentionWhores.forEachEntry((ind, value) -> {
			toPos(ind,pos);
			pos.set(centerX+pos.x,bottomY+pos.y,centerZ+pos.z).setBlock(world,value.block,value.meta,3);
			return true;
		});
		
		tileEntityMap.forEachEntry((ind, value) -> {
			toPos(ind,pos);
			pos.move(centerX,bottomY,centerZ);
			value.generateTile(pos.getTileEntity(world),rand);
			return true;
		});
		
		scheduledUpdates.forEach(ind -> {
			toPos(ind,pos);
			world.markBlockForUpdate(centerX+pos.getX(),bottomY+pos.getY(),centerZ+pos.getZ());
			return true;
		});
		
		entityList.forEach(entity -> {
			entity.setPosition(centerX+entity.posX,bottomY+entity.posY,centerZ+entity.posZ);
			entity.setWorld(world);
			world.spawnEntityInWorld(entity);
		});
	}
}
