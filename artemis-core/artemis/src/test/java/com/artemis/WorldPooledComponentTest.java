package com.artemis;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import com.artemis.component.CountingPooledComponent;
import org.junit.Test;

import com.artemis.component.ReusedComponent;
import com.artemis.systems.IteratingSystem;

public class WorldPooledComponentTest
{
	@Test
	public void pooled_component_reuse_with_deleted_entities() {
		World world = new World(new WorldConfiguration()
				.setSystem(new SystemComponentEntityRemover()));

		Set<Integer> hashes = runWorld(world);
		assertEquals("Contents: " + hashes, 3, hashes.size());
	}

	private Set<Integer> runWorld(World world) {
		Set<Integer> hashes = new HashSet<Integer>();
		hashes.add(createEntity(world));
		hashes.add(createEntity(world));
		world.process();
		hashes.add(createEntity(world));
		world.process();
		hashes.add(createEntity(world));
		world.process();
		hashes.add(createEntity(world));
		world.process();
		hashes.add(createEntity(world));
		hashes.add(createEntity(world));
		hashes.add(createEntity(world));
		world.process();
		world.process();
		hashes.add(createEntity(world));
		world.process();
		
		return hashes;
	}

	@Test
	public void creating_pooled_components_returns_old_to_pool() {
		World w = new World();
		int e = w.create();
		CountingPooledComponent cpc1 = w.edit(e).create(CountingPooledComponent.class);
		w.process();

		w.edit(e).create(CountingPooledComponent.class);
		w.process();

		assertEquals(cpc1, w.edit(e).create(CountingPooledComponent.class));
	}

	private int createEntity(World world)
	{
		int e = world.create();
		ReusedComponent component = world.edit(e).create(ReusedComponent.class);
		return Integer.hashCode(e);
	}
	
	static class SystemComponentEntityRemover extends IteratingSystem
	{
		@SuppressWarnings("unchecked")
		public SystemComponentEntityRemover()
		{
			super(Aspect.all(ReusedComponent.class));
		}

		@Override
		protected void process(int e)
		{
			world.delete(e);
		}
	}
	
	static class SystemComponentPooledRemover extends IteratingSystem
	{
		@SuppressWarnings("unchecked")
		public SystemComponentPooledRemover()
		{
			super(Aspect.all(ReusedComponent.class));
		}
		
		@Override
		protected void process(int e)
		{
			world.getMapper(ReusedComponent.class).remove(e);
		}
	}
}
