package com.artemis;

import com.artemis.annotations.SkipWire;
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import com.artemis.utils.IntDeque;

import com.artemis.utils.BitVector;

import static com.artemis.Aspect.all;

/**
 * Manages entity instances.
 *
 * @author Arni Arent
 * @author Adrian Papari
 */
@SkipWire
public class EntityManager extends BaseSystem {
	private final BitVector recycled = new BitVector();
	private final IntDeque limbo = new IntDeque();
	private int maxSize;
	private int nextID;
	protected int maxUsedID = -1;
	private Bag<BitVector> entityBitVectors = new Bag<BitVector>(BitVector.class);

	protected EntityManager(int initialContainerSize) {
		maxSize = initialContainerSize;
		registerEntityStore(recycled);
	}

	@Override
	protected void processSystem() {}

	protected int create() {
		return obtain();
	}

	void clean(IntBag pendingDeletion) {
		int[] ids = pendingDeletion.getData();
		for(int i = 0, s = pendingDeletion.size(); s > i; i++) {
			int id = ids[i];
			// usually never happens but:
			// this happens when an entity is deleted before
			// it is added to the world, ie; created and deleted
			// before World#process has been called
			if (!recycled.unsafeGet(id)) {
				free(id);
			}
		}
	}

	/**
	 * Check if this entity is active.
	 * <p>
	 * Active means the entity has been created and is not deleted.
	 * </p>
	 * 
	 * @param entityID
	 *			the entities id
	 *
	 * @return true if active, false if not
	 */
	public boolean isActive(int entityID) {
		return entityID <= maxUsedID && !recycled.unsafeGet(entityID);
	}
	
	public boolean isDeleted(int entityID) {
		return recycled.unsafeGet(entityID);
	}

	public void registerEntityStore(BitVector bv) {
		bv.ensureCapacity(maxSize);
		entityBitVectors.add(bv);
	}

	/**
	 * <p>If all entties have been deleted, resets the entity cache - with next entity
	 * entity receiving id <code>0</code>. There mustn't be any active entities in
	 * the world for this method to work. This method does nothing if it fails.</p>
	 *
	 * <p>For the reset to take effect, a new {@link World#process()} must initiate.</p>
	 *
	 * @return true if entity id was successfully reset.
	 *
	 */
	public boolean reset() {
		int count = world.getAspectSubscriptionManager().get(all()).getEntityCount();

		if (count > 0)
			return false;

		limbo.clear();
		recycled.clear();

		nextID = 0;
		maxUsedID = -1;

		return true;
	}
	
	public void setNextID(int nextID) {
		if (isActive(nextID)) {
			throw new IllegalStateException("entityID " + nextID + " is already in use");
		}
		
		this.nextID = nextID;
		limbo.clear();
		recycled.unsafeClear(nextID);
		
		if (nextID >= maxSize) {
			maxSize = getNextPowerOfTwo(nextID);
			growEntityStores();
		}
	}
	
	int getNextPowerOfTwo(int value) {
		int highestOneBit = Integer.highestOneBit(value);
		if (value == highestOneBit) {
			return value;
		}
		return highestOneBit << 1;
	}

	/**
	 * Instantiates an Entity without registering it into the world.
	 */
	private int createEntity() {
		if (nextID >= maxSize) {
			maxSize = 2 * maxSize;
			growEntityStores();
		}
		
		maxUsedID = nextID;

		return nextID++;
	}

	private void growEntityStores() {
		ComponentManager cm = world.getComponentManager();
		cm.ensureCapacity(maxSize);

		for (int i = 0, s = entityBitVectors.size(); s > i; i++) {
			entityBitVectors.get(i).ensureCapacity(maxSize);
		}
	}

	private int obtain() {
		if (limbo.isEmpty()) {
			return createEntity();
		} else {
			int id = limbo.popFirst();
			recycled.unsafeClear(id);
			return id;
		}
	}

	private void free(int entityID) {
		limbo.add(entityID);
		recycled.unsafeSet(entityID);
	}
}
