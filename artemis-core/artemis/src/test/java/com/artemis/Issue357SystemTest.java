package com.artemis;

import com.artemis.systems.IteratingSystem;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Snorre E. Brekke
 */
public class Issue357SystemTest {
	@Test
	public void test_two_systems_in_world_delete_during_process() {
		World world = new World(new WorldConfiguration().setSystem(TestSystemWithDelete.class)
				.setSystem(AnyOldeBaseSystem.class));
		world.edit(world.create()).create(TestComponent.class);
		world.process();

		world.edit(world.create()).create(TestComponent.class);
		world.process(); //This test fails in 0.13.0!

	}

	@Test
	public void test_one_system_in_world_delete_during_process() {
		World world = new World(new WorldConfiguration().setSystem(TestSystemWithDelete.class));
		world.edit(world.create()).create(TestComponent.class);
		world.process();

		world.edit(world.create()).create(TestComponent.class);
		world.process();
		//This test is ok i 0.13.0
	}

	@Test
	public void test_two_systems_in_world_delete_after_process() {
		World world = new World(new WorldConfiguration().setSystem(TestSystemWithoutDelete.class)
				.setSystem(AnyOldeBaseSystem.class));
		int entity = world.create();
		world.edit(entity).create(TestComponent.class);
		world.process();
		world.delete(entity);

		int entity2 = world.create();
		world.edit(entity2).create(TestComponent.class);
		world.process();
		world.delete(entity2);
		world.process();
		//This test is ok i 0.13.0
	}

	@Test
	public void test_two_systems_in_world_delete_before_process() {
		World world = new World(new WorldConfiguration()
			.setSystem(TestSystemWithoutDelete.class)
			.setSystem(AnyOldeBaseSystem.class));

		int entity = world.create();
		world.edit(entity).create(TestComponent.class);
		world.delete(entity);
		world.process();
		world.edit(world.create()).create(TestComponent.class);
		world.delete(entity);
		world.process();
		//This test is ok i 0.13.0
	}

	public static class TestSystemWithDelete extends IteratingSystem {
		private ComponentMapper<TestComponent> mapper;

		public TestSystemWithDelete() {
			super(Aspect.all(TestComponent.class));
		}

		@Override
		protected void process(int entity) {
			TestComponent testComponent = mapper.get(entity);
			assertNotNull("int with id <" + entity + "> has null component", testComponent);
			world.delete(entity);
		}
	}

	public static class TestSystemWithoutDelete extends IteratingSystem {
		private ComponentMapper<TestComponent> mapper;

		public TestSystemWithoutDelete() {
			super(Aspect.all(TestComponent.class));
		}

		@Override
		protected void process(int entity) {
			TestComponent testComponent = mapper.get(entity);
			assertNotNull("int with id <" + entity + "> has null component", testComponent);
		}
	}

	public static class AnyOldeBaseSystem extends BaseSystem {
		@Override
		protected void processSystem() {
		}
	}

	public static class TestComponent extends Component {
	}

}
