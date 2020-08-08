package com.artemis;

import static org.junit.Assert.assertEquals;

import com.artemis.ComponentManager.ComponentIdentityResolver;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.BitVector;
import org.junit.Assert;
import org.junit.Test;

import com.artemis.component.ComponentX;
import com.artemis.component.ComponentY;
import com.artemis.systems.DelayedIteratingSystem;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.Bag;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

public class WorldTest
{
	@Test
	public void sandbox() {
		System.out.println(VM.current().details());
		System.out.println();
		print(AspectSubscriptionManager.class);
		print(BaseEntitySystem.class);
		print(BatchChangeProcessor.class);
		print(ComponentIdentityResolver.class);
		print(BaseComponentMapper.class);
		print(ComponentMapper.class);
		print(ComponentType.class);
		print(ComponentPool.class);
		print(ComponentManager.class);
		print(DelayedComponentRemover.class);
		print(EntityManager.class);
		print(EntitySubscription.class);
		print(EntityTransmuter.class);
		print(EntityTransmuter.TransmuteOperation.class);
		print(ImmediateComponentRemover.class);
		print(IteratingSystem.class);
		print(SystemInvocationStrategy.class);
		print(World.class);
		print(BitVector.class);
	}

	protected void print(Class<?> klazz) {
		System.out.println(ClassLayout.parseClass(klazz).toPrintable());
	}

	@Test
	public void get_component_should_not_throw_exception()
	{
		World world = new World(new WorldConfiguration());

		for (int i = 0; i < 100; i++) {
			int e = world.create();
			if (i == 0) world.edit(e).add(new ComponentX());
		}

		world.process();

		for (int i = 0; i < 100; i++) {
			world.getMapper(ComponentX.class).get(i);
		}
	}

	@Test
	public void access_component_after_deletion_in_previous_system()
	{
		World world = new World(new WorldConfiguration()
				.setSystem(new SystemComponentXRemover())
				.setSystem(new SystemB()));

		int e = world.create();
		world.edit(e).create(ComponentX.class);
		
		world.process();
	}
	
	@Test
	public void delayed_entity_procesing_ensure_entities_processed()
	{
		ExpirationSystem es = new ExpirationSystem();
		World world = new World(new WorldConfiguration()
				.setSystem(es));

		int e1 = createEntity(world);
		
		world.setDelta(0.5f);
		world.process();
		assertEquals(0, es.expiredLastRound);
		
		int e2 = createEntity(world);
		
		world.setDelta(0.75f);
		world.process();
		assertEquals(1, es.expiredLastRound);
		assertEquals(0.25f, es.deltas.get(e2), 0.01f);
		world.delta = 0;
		world.process();
		assertEquals(1, es.getSubscription().getEntities().size());
		
		world.setDelta(0.5f);
		world.process();
		
		assertEquals(1, es.expiredLastRound);
		
		world.process();
		assertEquals(0, es.getSubscription().getEntities().size());
	}

	private int createEntity(World world)
	{
		int e = world.create();
		world.edit(e).create(ComponentY.class);
		return e;
	}

	static class SystemComponentXRemover extends IteratingSystem
	{
		@SuppressWarnings("unchecked")
		public SystemComponentXRemover()
		{
			super(Aspect.all(ComponentX.class));
		}

		@Override
		protected void process(int e)
		{
			world.edit(e).remove(ComponentX.class);
		}
	}

	static class SystemB extends IteratingSystem
	{
		ComponentMapper<ComponentX> xm;

		@SuppressWarnings("unchecked")
		public SystemB()
		{
			super(Aspect.all(ComponentX.class));
		}

		@Override
		protected void process(int e)
		{
			xm.get(e);
		}
	}
	
	static class SystemY extends IteratingSystem
	{
		ComponentMapper<ComponentY> ym;
		
		@SuppressWarnings("unchecked")
		public SystemY()
		{
			super(Aspect.all(ComponentY.class));
		}
		
		@Override
		protected void process(int e)
		{
			Assert.assertNotNull(ym);
			ym.get(e);
		}
	}
	
	static class ExpirationSystem extends DelayedIteratingSystem
	{
		// don't do this IRL
		private Bag<Float> deltas = new Bag<Float>();
		int expiredLastRound;

		@SuppressWarnings("unchecked")
		public ExpirationSystem() {
			super(Aspect.all(ComponentY.class));
		}
		
		@Override
		public void inserted(int e) {
			deltas.set(e, 1f);
			super.inserted(e);
		}
		
		@Override
		protected float getRemainingDelay(int e) {
			return deltas.get(e);
		}

		@Override
		protected void processDelta(int e, float accumulatedDelta) {
			float remaining = deltas.get(e);
			remaining -=  accumulatedDelta;
			offerDelay(remaining);
			deltas.set(e, remaining);
		}

		@Override
		protected void processExpired(int e) {
			expiredLastRound++;
			deltas.set(e, null);
			world.delete(e);
		}
		
		@Override
		protected void begin() {
			expiredLastRound = 0;
		}
	}
}
