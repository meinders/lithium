package lithium.books;

import java.net.*;

import junit.framework.*;

public class ReferenceTest extends TestCase {
	public void testReferenceFromURI() {
		System.out.println("ReferenceTest.testReferenceFromURI()");

		ReferenceFromURI[] testCases = {
		        new ReferenceFromURI("urn:opwviewer:book//TestLibrary",
		                "TestLibrary", null, 0, 0),
		        new ReferenceFromURI(
		                "urn:opwviewer:book//TestLibrary/TestBook",
		                "TestLibrary", "TestBook", 0, 0),
		        new ReferenceFromURI(
		                "urn:opwviewer:book//TestLibrary/TestBook/123",
		                "TestLibrary", "TestBook", 123, 0),
		        new ReferenceFromURI(
		                "urn:opwviewer:book//TestLibrary/TestBook/123/234",
		                "TestLibrary", "TestBook", 123, 234),
		        new ReferenceFromURI("urn:opwviewer:book/TestBook", null,
		                "TestBook", 0, 0),
		        new ReferenceFromURI("urn:opwviewer:book/TestBook/123", null,
		                "TestBook", 123, 0),
		        new ReferenceFromURI("urn:opwviewer:book/TestBook/123/234",
		                null, "TestBook", 123, 234),
		        new ReferenceFromURI("", IllegalArgumentException.class),
		        new ReferenceFromURI("http://www.acme.com/",
		                IllegalArgumentException.class),
		        new ReferenceFromURI("urn:", IllegalArgumentException.class),
		        new ReferenceFromURI("urn:://", IllegalArgumentException.class),
		        new ReferenceFromURI("urn:opwviewer",
		                IllegalArgumentException.class),
		        new ReferenceFromURI("urn:opwviewer:",
		                IllegalArgumentException.class),
		        new ReferenceFromURI("urn:opwviewer:book",
		                IllegalArgumentException.class),
		        new ReferenceFromURI("urn:opwviewer:book/",
		                IllegalArgumentException.class),
		        new ReferenceFromURI("urn:opwviewer:book//",
		                IllegalArgumentException.class),
		        new ReferenceFromURI(
		                "urn:opwviewer:book//TestLibrary/TestBook/-123",
		                IllegalArgumentException.class),
		        new ReferenceFromURI(
		                "urn:opwviewer:book//TestLibrary/TestBook/123/-123",
		                IllegalArgumentException.class),
		        new ReferenceFromURI(
		                "urn:opwviewer:book//TestLibrary/TestBook/123/123/123",
		                IllegalArgumentException.class), };

		for (ReferenceFromURI testCase : testCases) {
			System.out.println(" - '" + testCase.uri + "'");
			testCase.assertTestCase();
		}
	}

	private static class ReferenceFromURI {
		private String uri;

		private String library;

		private String book;

		private int chapter;

		private int verse;

		private Class<? extends Throwable> exceptionClass;

		public ReferenceFromURI(String uri, String library, String book,
		        int chapter, int verse) {
			super();
			this.uri = uri;
			this.library = library;
			this.book = book;
			this.chapter = chapter;
			this.verse = verse;
		}

		public ReferenceFromURI(String uri,
		        Class<? extends Throwable> exceptionClass) {
			super();
			this.uri = uri;
			this.exceptionClass = exceptionClass;
		}

		public void assertTestCase() {
			if (exceptionClass == null) {
				Reference reference = new Reference(URI.create(uri));
				assertEquals("Unexpected library.", library,
				        reference.getLibrary());
				assertEquals("Unexpected book.", book, reference.getBook());
				assertEquals("Unexpected chapter.", chapter,
				        reference.getChapter());
				assertEquals("Unexpected verse.", verse, reference.getVerse());
				assertEquals("URI mismatch.", uri.replaceAll(":opwviewer:",
				        ":X-opwviewer:"), reference.toURI().toString());
			} else {
				try {
					new Reference(URI.create(uri));
					throw new AssertionFailedError("Expected exception of "
					        + exceptionClass);
				} catch (Throwable t) {
					if (t instanceof AssertionFailedError) {
						throw (AssertionFailedError) t;
					} else {
						assertEquals("Unexpected exception.", exceptionClass,
						        t.getClass());
					}
				}
			}
		}
	}
}
