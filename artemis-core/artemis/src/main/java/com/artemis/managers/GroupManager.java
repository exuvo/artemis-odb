package com.artemis.managers;

import static com.artemis.Aspect.all;

import java.util.HashMap;
import java.util.Map;

import com.artemis.BaseSystem;
import com.artemis.EntitySubscription;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;

/**
 * If you need to group your entities together, e.g tanks going into "units"
 * group or explosions into "effects", then use this manager.
 * <p>
 * You must retrieve it using world instance.
 * </p>
 * <p>
 * A entity can be assigned to more than one group.
 * </p>
 *
 * @author Arni Arent
 */
public class GroupManager extends BaseSystem {
    private static final ImmutableBag<String> EMPTY_BAG = new Bag<>();

    /**
     * All entities and groups mapped with group names as key.
     */
    private final Map<String, IntBag> identitiesByGroup;
    /**
     * All entities and groups mapped with entityID as key.
     */
    private final Map<Integer, Bag<String>> groupsByEntity;

    /**
     * Creates a new GroupManager instance.
     */
    public GroupManager() {
        identitiesByGroup = new HashMap<>();
        groupsByEntity = new HashMap<>();
    }

    @Override
    protected void processSystem() {
    }

    @Override
    protected void initialize() {
        world.getAspectSubscriptionManager().get(all())
                .addSubscriptionListener(new EntitySubscription.SubscriptionListener() {
                    @Override
                    public void inserted(IntBag entities) {
                    }

                    @Override
                    public void removed(IntBag entities) {
                        deleted(entities);
                    }
                });
    }

    /**
     * Set the group of the entity.
     *
     * @param group group to add the entity into
     * @param entityID     entity to add into the group
     */
    public void add(int entityID, String group) {
        IntBag identities = identitiesByGroup.get(group);
        if (identities == null) {
            identities = new IntBag();
            identitiesByGroup.put(group, identities);
        }
        if (!identities.contains(entityID)) {
            identities.add(entityID);
        }

        Bag<String> groups = groupsByEntity.get(entityID);
        if (groups == null) {
            groups = new Bag<>();
            groupsByEntity.put(entityID, groups);
        }
        if (!groups.contains(group)) {
            groups.add(group);
        }
    }

    /**
     * Set the group of the entity.
     *
     * @param g1 group to add the entity into
     * @param g2 group to add the entity into
     * @param entityID  entity to add into the group
     */

    public void add(int entityID, String g1, String g2) {
        add(entityID, g1);
        add(entityID, g2);
    }

    /**
     * Set the group of the entity.
     *
     * @param g1 group to add the entity into
     * @param g2 group to add the entity into
     * @param g3 group to add the entity into
     * @param entityID  entity to add into the group
     */

    public void add(int entityID, String g1, String g2, String g3) {
        add(entityID, g1);
        add(entityID, g2);
        add(entityID, g3);
    }

    /**
     * Set the group of the entity.
     *
     * @param groups groups to add the entity into
     * @param entityID      entity to add into the group
     */

    public void add(int entityID, String... groups) {
        for (String group : groups) {
            add(entityID, group);
        }
    }

    /**
     * Remove the entity from the specified group.
     *
     * @param entityID     entity to remove from group
     * @param group group to remove the entity from
     */
    public void remove(int entityID, String group) {
        final IntBag identities = identitiesByGroup.get(group);
        if (identities != null) {
            identities.removeValue(entityID);
        }

        Bag<String> groups = groupsByEntity.get(entityID);
        if (groups != null) {
            groups.remove(group);
            if (groups.size() == 0)
                groupsByEntity.remove(entityID);
        }
    }

    /**
     * Removes the entity from the specified groups.
     *
     * @param entityID  entity to remove from group
     * @param g1 group to remove the entity from
     * @param g2 group to remove the entity from
     */

    public void remove(int entityID, String g1, String g2) {
        remove(entityID, g1);
        remove(entityID, g2);
    }

    /**
     * Removes the entity from the specified groups.
     *
     * @param entityID  entity to remove from group
     * @param g1 group to remove the entity from
     * @param g2 group to remove the entity from
     * @param g3 group to remove the entity from
     */

    public void remove(int entityID, String g1, String g2, String g3) {
        remove(entityID, g1);
        remove(entityID, g2);
        remove(entityID, g3);
    }

    /**
     * Removes the entity from the specified groups
     *
     * @param entityID      entity to remove from group
     * @param groups groups to remove the entity from
     */

    public void remove(int entityID, String... groups) {
        for (String group : groups) {
            remove(entityID, group);
        }
    }

    /**
     * Remove the entity from all groups.
     *
     * @param entityID the entity to remove
     */
    public void removeFromAllGroups(int entityID) {
        Bag<String> groups = groupsByEntity.get(entityID);
        if (groups == null)
            return;
        for (int i = 0, s = groups.size(); s > i; i++) {
            IntBag identities = identitiesByGroup.get(groups.get(i));
            if (identities != null) {
                identities.removeValue(entityID);
            }
        }
        groupsByEntity.remove(entityID);
    }

    /**
     * Get all entities that belong to the provided group.
     *
     * @param group name of the group
     * @return read-only bag of entities belonging to the group
     */
    public ImmutableIntBag getEntities(String group) {
        IntBag entities = identitiesByGroup.get(group);
        if (entities == null) {
            entities = new IntBag();
            identitiesByGroup.put(group, entities);
        }
        return entities;
    }

    public ImmutableIntBag getEntityIds(final String group) {
        IntBag identities = identitiesByGroup.get(group);
        if (identities == null) {
            identities = new IntBag();
            identitiesByGroup.put(group, identities);
        }
        return identities;
    }

    /**
     * Get all groups the entity belongs to. An empty Bag is returned if the
     * entity doesn't belong to any groups.
     *
     * @param entityID the entity
     * @return the groups the entity belongs to.
     */
    public ImmutableBag<String> getGroups(int entityID) {
        Bag<String> groups = groupsByEntity.get(entityID);
        return groups != null ? groups : EMPTY_BAG;
    }

    /**
     * Checks if the entity belongs to any group.
     *
     * @param entityID the entity to check
     * @return true. if it is in any group, false if none
     */
    public boolean isInAnyGroup(int entityID) {
        return getGroups(entityID).size() > 0;
    }

    /**
     * Check if the entity is in the supplied group.
     *
     * @param group the group to check in
     * @param entityID     the entity to check for
     * @return true if the entity is in the supplied group, false if not
     */
    public boolean isInGroup(int entityID, String group) {
        if (group != null) {
            Bag<String> bag = groupsByEntity.get(entityID);
            if (bag != null) {
                Object[] groups = bag.getData();
                for (int i = 0, s = bag.size(); s > i; i++) {
                    String g = (String) groups[i];
                    if (group.equals(g)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void deleted(IntBag entities) {
        int[] ids = entities.getData();
        for (int i = 0, s = entities.size(); s > i; i++) {
            removeFromAllGroups(ids[i]);
        }
    }
}
