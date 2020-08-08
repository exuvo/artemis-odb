package com.artemis;

import static org.junit.Assert.*;

import org.junit.Test;

import com.artemis.systems.IteratingSystem;

@SuppressWarnings("static-method")
public class Issue206SystemTest {

	@Test
	public void test_edited_bitset_sanity() {
		World world = new World(new WorldConfiguration()
				.setSystem(new TestSystemAB()));

		int e = world.create();
		world.edit(e).create(CompA.class);
		world.edit(e).create(CompB.class);
		world.edit(e).create(TestComponentC.class);

		world.process();
		
//		assertSame(world.edit(e), e.edit());
		world.edit(e).remove(CompB.class);
		// nota bene: in 0.7.0 and 0.7.1, chaining edit() caused
		// the componentBits to reset
		world.edit(e).remove(TestComponentC.class);

		world.process();
		world.process();
	}

	public static class CompA extends Component {}
	public static class CompB extends Component {}
	public static class TestComponentC extends Component {}

	private static class TestSystemAB extends IteratingSystem {
		@SuppressWarnings("unchecked")
		public TestSystemAB() {
			super(Aspect.all(CompA.class, CompB.class));
		}

		@Override
		protected void process(int e) {
			assertNotNull(world.getMapper(CompB.class).get(e));
		}
	}
}
