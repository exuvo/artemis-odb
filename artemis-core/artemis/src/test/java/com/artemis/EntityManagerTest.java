package com.artemis;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.artemis.component.ComponentX;

import static org.junit.Assert.*;

public class EntityManagerTest {
	
	private World world;

	@Before
	public void setup() {
		world = new World();
	}
	
	@Test
	public void old_entities_are_recycled() {
		Set<Integer> ids = new HashSet<Integer>();
		
		int e1 = world.create();
		int e2 = world.create();
		int e3 = world.create();
		
		ids.add(System.identityHashCode(e1));
		ids.add(System.identityHashCode(e2));
		ids.add(System.identityHashCode(e3));
		
		assertEquals(3, ids.size());
		
		world.delete(e1);
		world.delete(e2);
		world.delete(e3);
		
		world.process();
		
		int e1b = world.create();
		int e2b = world.create();
		int e3b = world.create();

		ids.add(System.identityHashCode(e1b));
		ids.add(System.identityHashCode(e2b));
		ids.add(System.identityHashCode(e3b));
		
		assertEquals(ids.toString(), 3, ids.size());
	}
	
	@Test
	public void is_active_check_never_throws() {
		EntityManager em = world.getEntityManager();
		for (int i = 0; 1024 > i; i++) {
			int e = world.create();
			assertTrue(em.isActive(e));
		}
	}
	
	@Test
	public void recycled_entities_behave_nicely_with_components() {
		ComponentMapper<ComponentX> mapper = world.getMapper(ComponentX.class);
		
		int e1 = world.create();
		world.edit(e1).add(new ComponentX());
		assertTrue(mapper.has(e1));
		
		world.delete(e1);

		int e2 = world.create();
		
		assertNotEquals(e1, e2);
		assertFalse("Error:" + mapper.get(e2), mapper.has(e2));
	}
	
	@Test
	public void should_recycle_entities_after_one_round() {
		ComponentMapper<ComponentX> mapper = world.getMapper(ComponentX.class);
		
		int e1 = world.create();
		world.edit(e1).add(new ComponentX());
		assertTrue(mapper.has(e1));
		
		world.delete(e1);
		world.process();
		int e2 = world.create();

		assertEquals(e1, e2);
		assertFalse("Error:" + mapper.get(e2), mapper.has(e2));
	}

	@Test
	public void reset_entity_cache() {
		World w = new World(new WorldConfiguration());
		int[] ids = new int[] { w.create(), w.create(), w.create() };

		assertArrayEquals(new int[] {0, 1, 2}, ids);

		w.process();

		w.delete(2);
		w.delete(1);
		w.delete(0);

		w.process();

		boolean successfullReset = w.getEntityManager().reset();
		assertTrue(successfullReset);

		w.process();

		ids = new int[] { w.create(), w.create(), w.create() };
		assertArrayEquals(new int[] {0, 1, 2}, ids);
	}
}
