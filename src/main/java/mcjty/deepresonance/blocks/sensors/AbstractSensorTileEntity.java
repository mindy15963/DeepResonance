package mcjty.deepresonance.blocks.sensors;

import mcjty.deepresonance.blocks.crystals.ResonatingCrystalTileEntity;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSensorTileEntity extends GenericTileEntity implements ITickable {

    private int power;
    private BlockPos crystalPos;

    // We enqueue sensors for processing later
    public static Set<AbstractSensorTileEntity> todoSensors = new HashSet<>();

    @Override
    public void update() {
        if (!world.isRemote) {
            todoSensors.add(this);
        }
    }

    public void realUpdate() {
        int sensor = checkSensor();
        System.out.println("Sensor=" + sensor);
        setRedstoneOut(sensor);
    }

    @Nullable
    protected ResonatingCrystalTileEntity getCrystal() {
        while (true) {
            if (crystalPos == null) {
                findCrystal();
                if (crystalPos == null) {
                    return null;
                }
            }
            TileEntity te = world.getTileEntity(crystalPos);
            if (te instanceof ResonatingCrystalTileEntity) {
                return (ResonatingCrystalTileEntity) te;
            }
            crystalPos = null;
        }
    }

    private void findCrystal() {
        BlockPos p = pos.down();
        // @todo config max range
        for (int i = 0 ; i < 8 ; i++) {
            TileEntity te = world.getTileEntity(p);
            if (te instanceof ResonatingCrystalTileEntity) {
                crystalPos = p;
                return;
            }
            p = p.down();
        }
        crystalPos = null;
    }

    protected abstract int checkSensor();

    public int getPower() {
        return power;
    }

    private void setRedstoneOut(int a) {
        if (power == a) {
            return;
        }
        power = a;
        markDirtyQuick();
        getWorld().notifyNeighborsOfStateChange(this.pos, this.getBlockType(), false);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        power = tagCompound.getInteger("power");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("power", power);
        return tagCompound;
    }
}
