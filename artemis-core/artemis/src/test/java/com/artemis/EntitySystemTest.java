package com.artemis;

import static org.junit.Assert.assertEquals;

import com.artemis.systems.IteratingSystem;
import org.junit.Test;

/**
 * Created by obartley on 6/9/14.
 */
public class EntitySystemTest {

	@SuppressWarnings("static-method")
	@Test
	public void test_process_one_active() {
		World w = new World(new WorldConfiguration()
			.setSystem(new IteratorTestSystem(1)));

		int e = w.create();
		w.edit(e).add(new C());

		w.process();
	}

	@Test
	public void aspect_exclude_only() {
		ExcludingSystem es1 = new ExcludingSystem();
		EmptySystem es2 = new EmptySystem();
		World w = new World(new WorldConfiguration()
				.setSystem(es1)
				.setSystem(es2));

		int e = w.create();
		w.process();

		assertEquals(1, es1.getSubscription().getEntities().size());
		assertEquals(1, es2.getSubscription().getEntities().size());
	}

	public static class C extends Component {}
	public static class C2 extends Component {}

	public static class IteratorTestSystem extends BaseEntitySystem {
		public int expectedSize;
		
		@SuppressWarnings("unchecked")
		public IteratorTestSystem(int expectedSize) {
			super(Aspect.all(C.class));
			this.expectedSize = expectedSize;
		}

		@Override
		protected void processSystem() {
			assertEquals(expectedSize, subscription.getEntities().size());
			//getSubscription().getEntities();
		}

		@Override
		protected boolean checkProcessing() {
			return true;
		}
	}

	public static class ExcludingSystem extends IteratingSystem {
		public ExcludingSystem() {
			super(Aspect.exclude(C.class));
		}

		@Override
		protected void process(int e) {}
	}

	public static class EmptySystem extends IteratingSystem {
		public EmptySystem() {
			super(Aspect.all());
		}

		@Override
		protected void process(int e) {}
	}
}
