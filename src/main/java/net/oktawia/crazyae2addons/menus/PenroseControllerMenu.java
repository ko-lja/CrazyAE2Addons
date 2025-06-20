package net.oktawia.crazyae2addons.menus;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.RestrictedInputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.PenroseControllerBE;


public class PenroseControllerMenu extends AEBaseMenu {

    private final PenroseControllerBE host;
    private final AppEngSlot singularitySlot;
    public final FakeSlot configSlot;
    public final RestrictedInputSlot diskSlot;
    public String EXTRACT = "actionExtract";
    public String INSERT = "actionInsert";
    public String POWER = "actionPower";
    @GuiSync(93)
    public boolean powerMode;

    public PenroseControllerMenu(int id, Inventory ip, PenroseControllerBE host) {
        super(CrazyMenuRegistrar.PENROSE_CONTROLLER_MENU.get(), id, ip, host);
        this.createPlayerInventorySlots(ip);
        this.host = host;
        this.powerMode = host.energyMode;
        this.addSlot(this.diskSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.STORAGE_CELLS, host.diskInv, 0), SlotSemantics.STORAGE_CELL);
        this.addSlot(this.singularitySlot = new AppEngSlot(host.inputInv, 0), SlotSemantics.MACHINE_INPUT);
        this.addSlot(this.configSlot = new FakeSlot(host.config.createMenuWrapper(), 0), SlotSemantics.CONFIG);
        this.registerClientAction(EXTRACT, this::extractFromCell);
        this.registerClientAction(INSERT, this::insertToCell);
        this.registerClientAction(POWER, Boolean.class, this::changeEnergyMode);
    }

    public void extractFromCell(){
        if (isClientSide()){
            sendClientAction(EXTRACT);
        } else {
            StorageCell disk = StorageCells.getCellInventory(host.diskInv.getStackInSlot(0), null);
            if (disk == null) return;
            int toExtract = this.singularitySlot.getInventory().isEmpty() ? 64 : 64 - this.singularitySlot.getItem().getCount();
            var extracted = disk.extract(AEItemKey.of(CrazyItemRegistrar.SUPER_SINGULARITY.get()), toExtract, Actionable.MODULATE, IActionSource.ofMachine(host));
            if (this.singularitySlot.getInventory().isEmpty()){
                var stack = CrazyItemRegistrar.SUPER_SINGULARITY.get().getDefaultInstance();
                stack.setCount((int) extracted);
                this.singularitySlot.set(stack);
            } else {
                this.singularitySlot.getItem().setCount((int) (this.singularitySlot.getItem().getCount() + extracted));
            }
        }
    }

    public void insertToCell(){
        if (isClientSide()){
            sendClientAction(INSERT);
        } else {
            StorageCell disk = StorageCells.getCellInventory(host.diskInv.getStackInSlot(0), null);
            if (disk == null) return;
            var inserted = disk.insert(AEItemKey.of(this.singularitySlot.getItem()), this.singularitySlot.getItem().getCount(), Actionable.MODULATE, IActionSource.ofMachine(host));
            this.singularitySlot.getItem().setCount((int) (this.singularitySlot.getItem().getCount() - inserted));
        }
    }

    public void changeEnergyMode(boolean dir){
        this.powerMode = dir;
        this.host.energyMode = dir;
        if (isClientSide()){
            sendClientAction(POWER, dir);
        }
    }
}
