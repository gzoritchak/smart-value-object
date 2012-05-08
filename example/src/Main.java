
import org.bsf.smartValueObject.SmartAccess;

public class Main {
	public static void main(String[] args) {
		ExampleVO vo = new ExampleVO(0);
		SmartAccess.reset(vo);
		// vo should be 'clean'
		checkModification(vo);

		vo.setId(10);
		// now it's 'dirty'
		checkModification(vo);

		// reset again to clean
		SmartAccess.reset(vo);

		vo.setId(10);
		// object should still be clean (no changes made)
		checkModification(vo);

		// add some VOs to container
		vo.add(new ExampleVO(1));
		vo.add(new ExampleVO(2));
		vo.add(new ExampleVO(3));

		ExampleVO toBeRemoved = new ExampleVO(4);
		// we need to reset the state otherwise it wouldn't get removed
		// (objects flagged as 'new' get really deleted in the container)
		SmartAccess.reset(toBeRemoved);
		vo.add(toBeRemoved);
		vo.remove(toBeRemoved);

		System.out.println("created objects");
		for (java.util.Iterator it = 
				SmartAccess.createdIterator(vo.getCollection());
				it.hasNext(); ) {
			System.out.println(it.next());
		}

		System.out.println("deleted objects");
		for (java.util.Iterator it = 
				SmartAccess.deletedIterator(vo.getCollection());
				it.hasNext(); ) {
			System.out.println(it.next());
		}
	}

	public static void checkModification(Object o) {
		if (SmartAccess.isDirty(o)) {
			System.out.println("object " + o + " has been modified");
		}
	}
}
