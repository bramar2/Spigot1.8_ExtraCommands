package me.bramar.extracommands.events;

import org.bukkit.entity.Item;

import java.util.*;
import java.util.function.Consumer;

public class ItemList extends ArrayList<Item> {
    private final List<Item> allItems = new ArrayList<>();
    private final List<Consumer<Item>> consumers = new ArrayList<>();
    private boolean disposed = false;
    private final int id;
    public boolean same(List<Item> itemList) {
        if(itemList == this) return true;
        if(itemList == null) return false;
        return ((ItemList) itemList).id == this.id;
    }
    public ItemList(Collection<? extends Item> c) {
        // add, addAll, replaceAll, retainAll, set
        super();
        addAll(c);
        id = new Random().nextInt(Short.MAX_VALUE);
    }
    public ItemList(Item... content) {
        super();
        addAll(Arrays.asList(content));
        id = new Random().nextInt(Short.MAX_VALUE);
    }

    @Override
    public boolean add(Item item) {
        allItems.add(item);
        return super.add(item);
    }

    @Override
    public void add(int index, Item element) {
        allItems.add(element);
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends Item> c) {
        allItems.addAll(c);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Item> c) {
        allItems.addAll(c);
        return super.addAll(index, c);
    }

    @Override
    public Item set(int index, Item element) {
        allItems.add(element);
        return super.set(index, element);
    }

    // Despawns unused Item entities (Entities that are clear-ed/removed)
    public void despawnUnused() {
        updateDupe();
        allItems.forEach((item) -> {if(item != null && !contains(item)) item.remove();});
    }
    // Despawns all Item entities
    public void despawnAll() {
        updateDupe();
        allItems.forEach((item) -> {if(item != null) item.remove();});
    }
    // Update duplicate items
    private void updateDupe() {
        List<Item> newList = new ArrayList<>(new HashSet<>(allItems));
        allItems.clear();
        allItems.addAll(newList);
    }
    protected void doWhenDisposed(Consumer<Item> consumer) {
        consumers.add(consumer);
    }
    // Illegal implementation by enchantments
    public void runConsumers() {
        if(disposed) throw new UnsupportedOperationException(); // It would run twice if it is runned
        disposed = true;
        forEach(item -> {
            if(item != null && item.isValid()) consumers.forEach(consumer -> consumer.accept(item));
        });
    }
}
