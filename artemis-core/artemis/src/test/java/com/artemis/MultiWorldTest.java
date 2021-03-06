package com.artemis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.artemis.annotations.SkipWire;
import org.junit.Test;

import com.artemis.component.ComponentX;
import com.artemis.component.ComponentY;
import com.artemis.systems.VoidEntitySystem;

public class MultiWorldTest
{
	private World world;

	@Test
	public void uniqie_component_ids_per_world()
	{
		World innerWorld = new World(new WorldConfiguration()
				.setSystem(new EmptySystem()));

		world = new World(new WorldConfiguration()
				.setSystem(new InnerWorldProcessingSystem(innerWorld)));

		world.edit(world.create()).create(ComponentX.class);
		innerWorld.edit(innerWorld.create()).create(ComponentY.class);
		innerWorld.edit(innerWorld.create()).create(ComponentX.class);
		
		world.process();
		
		ComponentType xOuterType = world.getComponentManager().typeFactory.getTypeFor(ComponentX.class);
		ComponentType xInnerType = innerWorld.getComponentManager().typeFactory.getTypeFor(ComponentX.class);
		int xIndexOuter = xOuterType.getIndex();
		int xIndexInner = xInnerType.getIndex();
		int yIndexInner = 
				innerWorld.getComponentManager().typeFactory.getTypeFor(ComponentY.class).getIndex();
		
		assertEquals(xOuterType, world.getComponentManager().typeFactory.getTypeFor(xOuterType.getIndex()));
		assertEquals(xInnerType, innerWorld.getComponentManager().typeFactory.getTypeFor(xInnerType.getIndex()));
		assertNotEquals(xIndexOuter, xIndexInner);
		assertEquals(xIndexOuter, yIndexInner);
	}
	
	public static class EmptySystem
			extends VoidEntitySystem {

		@Override
		protected void processSystem() {
		}

	}

	public static class InnerWorldProcessingSystem
			extends VoidEntitySystem {

		@SkipWire
		private final World inner;

		public InnerWorldProcessingSystem(World inner) {
			super();
			this.inner = inner;
		}

		@Override
		protected void processSystem() {
			inner.delta = world.delta;
			inner.process();
		}
		
	}
	
	private static class VoidSystem extends VoidEntitySystem {
		@Override
		protected void processSystem() {}
	}
}
