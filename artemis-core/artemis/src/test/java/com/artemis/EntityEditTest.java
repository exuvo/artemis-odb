package com.artemis;


import static com.artemis.Aspect.all;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.artemis.utils.IntBag;
import org.junit.Test;

import com.artemis.component.ComponentX;
import com.artemis.component.ComponentY;

public class EntityEditTest {
	
	@SuppressWarnings("static-method")
	@Test
	public void basic_entity_edit_test() {
		LeManager lm = new LeManager();
		World world = new World(new WorldConfiguration().setSystem(lm));

		int e = world.create();
		world.process();
		
		assertEquals(1, lm.added);

		EntityEdit edit = world.edit(e);
		edit.create(ComponentX.class);
		edit.create(ComponentY.class);
		
		world.process();
		
		assertEquals(1, lm.added);
	}
	
	@Test
	public void test_composition_identity_simple_case() {
		World world = new World();

		int e = world.create();
		world.process();
		assertEquals(0, world.getComponentManager().getIdentity(e));
	}
	
	@Test
	public void test_composition_identity() {
		World world = new World();

		int e = world.create();
		assertEquals(0, world.getComponentManager().getIdentity(e));
	}

	@Test
	public void test_edit_right_after_delete_must_not_trigger_insertions() {
		World w = new World(new WorldConfiguration());

		int id = w.create();

		w.process();

		w.getAspectSubscriptionManager()
			.get(all(ComponentX.class))
			.addSubscriptionListener(new EntitySubscription.SubscriptionListener() {
				@Override
				public void inserted(IntBag entities) {
					fail("shouldn't be here...");
				}

				@Override
				public void removed(IntBag entities) {
					fail("shouldn't have time to be here...");
				}
			});


		w.delete(id);
		w.edit(id).create(ComponentX.class);

		w.process();
	}


	
	private static class LeManager extends BaseEntitySystem {
		int added;
		
		public LeManager() {
			super(Aspect.all());
		}

		@Override
		public void inserted(int e) {
			added++;
		}
		
		@Override
		protected void processSystem() {
		
		}
	}

}
