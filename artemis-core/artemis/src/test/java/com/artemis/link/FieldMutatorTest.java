package com.artemis.link;

import com.artemis.World;
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.Field;
import com.artemis.utils.reflect.ReflectionException;
import org.junit.Test;


import static org.junit.Assert.*;

public class FieldMutatorTest {
	@Test
	public void read_entity_id() throws Exception {
		LinkFactoryTest.LttEntityId c = new LinkFactoryTest.LttEntityId();
		c.id = 1337;

		IntFieldMutator mutator = new IntFieldMutator();
		assertEquals(1337, mutator.read(c, field(c, "id")));
	}

	@Test
	public void write_entity_id() throws Exception {
		LinkFactoryTest.LttEntityId c = new LinkFactoryTest.LttEntityId();

		IntFieldMutator mutator = new IntFieldMutator();
		mutator.write(1337, c, field(c, "id"));

		assertEquals(1337, mutator.read(c, field(c, "id")));
		assertEquals(1337, c.id);
	}

	@Test
	public void read_int_bag() throws Exception {
		World w = new World();
		LinkFactoryTest.LttIntBag c = new LinkFactoryTest.LttIntBag();
		c.ids.add(20);
		c.ids.add(30);
		c.ids.add(40);

		IntBag ids = new IntBag();
		ids.addAll(c.ids);

		IntBagFieldMutator mutator = new IntBagFieldMutator();
		mutator.setWorld(w);
		assertEquals(ids, mutator.read(c, field(c, "ids")));

		c.ids.removeIndex(1);
		ids.removeIndex(1);

		assertEquals(ids, mutator.read(c, field(c, "ids")));
	}

	private static Field field(Object object, String field) throws ReflectionException {
		return ClassReflection.getDeclaredField(object.getClass(), field);
	}
}