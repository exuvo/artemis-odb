package com.artemis;

import com.artemis.annotations.DelayedComponentRemoval;

public abstract class BaseComponentMapper<A extends Component> {
	/** The type of components this mapper handles. */
	public final ComponentType type;

	protected BaseComponentMapper(ComponentType type) {
		this.type = type;
	}

	/**
	 * Returns a component mapper for this type of components.
	 *
	 * @param <T>   the class type of components
	 * @param type  the class of components this mapper uses
	 * @param world the world that this component mapper should use
	 * @return a new mapper
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Component> BaseComponentMapper<T> getFor(Class<T> type, World world) {
		return world.getMapper(type);
	}

	/**
	 * Fast but unsafe retrieval of a component for this entity.
	 *
	 * This method trades performance for safety.
	 *
	 * User is expected to avoid calling this method on recently (in same system) removed components
	 * or invalid entity ids. Might return null, throw {@link ArrayIndexOutOfBoundsException}
	 * or a partially recycled component if called on in-system removed components.
	 *
	 * Only exception are components marked with {@link DelayedComponentRemoval}, when calling
	 * this method from within a subscription listener.
	 *
	 * @param entityID the entity that should possess the component
	 * @return the instance of the component.
	 * @throws ArrayIndexOutOfBoundsException if the component was removed or never existed
	 */
	public abstract A get(int entityID);

	/**
	 * Checks if the entity has this type of component.
	 *
	 * @param entityID the entity to check
	 * @return true if the entity has this component type, false if it doesn't
	 * @throws ArrayIndexOutOfBoundsException if the component was removed or never existed
	 */
	public abstract boolean has(int entityID);

	/**
	 * Remove component from entity.
	 * Does nothing if already removed.
	 *
	 * @param entityID to remove.
	 */
	public abstract void remove(int entityID);

	protected abstract void internalRemove(int entityID);
	
	/**
	 * Create component for this entity.
	 * Will avoid creation if component preexists.
	 *
	 * @param entityID the entity that should possess the component
	 * @return the instance of the component.
	 */
	public abstract A create(int entityID);

	public abstract A internalCreate(int entityID);

	/**
	 * Fast and safe retrieval of a component for this entity.
	 * If the entity does not have this component then fallback is returned.
	 *
	 * @param entityID Entity that should possess the component
	 * @param fallback fallback component to return, or {@code null} to return null.
	 * @return the instance of the component
	 */
	public A getSafe(int entityID, A fallback) {
		final A c = get(entityID);
		return (c != null) ? c : fallback;
	}

	/**
	 * Create or remove a component from an entity.
	 *
	 * Does nothing if already removed or created respectively.
	 *
	 * @param entityID Entity id to change.
	 * @param value {@code true} to create component (if missing), {@code false} to remove (if exists).
	 * @return the instance of the component, or {@code null} if removed.
	 */
	public A set(int entityID, boolean value) {
		if ( value ) {
			return create(entityID);
		} else {
			remove(entityID);
			return null;
		}
	}

	/**
	 * Returns the ComponentType of this ComponentMapper.
	 * see {@link ComponentMapper#type}
	 */
	public ComponentType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "ComponentMapper[" + type.getType().getSimpleName() + ']';
	}
}
