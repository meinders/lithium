package lithium.display;

import lithium.*;
import lithium.animation.legacy.scrolling.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link ViewModel}.
 *
 * @author Gerrit Meinders
 */
public class LyricViewModelTest
{
	protected ViewModel lvm;

	protected PropertyChangeTestListener testListener;

	@Before
	public void setUp()
	{
		// restore global playlist to initial state
		PlaylistManager.setPlaylist(new Playlist());

		lvm = new ViewModel();
		lvm.register(this);
		testListener = new PropertyChangeTestListener();
		lvm.addPropertyChangeListener(testListener);
	}

	@After
	public void tearDown()
	{
		if (lvm != null)
		{
			lvm.unregister(this);
			lvm.removePropertyChangeListener(testListener);
		}
	}

	@Test
	public void testLyricViewModel()
	{
		final Playlist global = new Playlist();
		PlaylistManager.setPlaylist(global);
		final ViewModel lvm = new ViewModel();
		assertSame("Expected global playlist", global, lvm.getPlaylist());
	}

	@Test
	public void testLyricViewModelBoolean()
	{
		final Playlist global = new Playlist();
		PlaylistManager.setPlaylist(global);
		final ViewModel lvm = new ViewModel(true);
		assertNotNull("Expected playlist", lvm.getPlaylist());
		assertTrue("Expected empty playlist", lvm.getPlaylist().isEmpty());
		assertNotSame("Expected global playlist", global, lvm.getPlaylist());
	}

	@Test
	public void testRegisterUnregister()
	{
		final Object user1 = this;
		final Object user2 = new Object();

		// user1 is registered by #setUp()
		lvm.register(user2);
		assertFalse("Unexpected disposal", lvm.isDisposed());
		lvm.unregister(user1);
		assertFalse("Unexpected disposal", lvm.isDisposed());
		lvm.unregister(user2);
		assertTrue("Expected disposal", lvm.isDisposed());
	}

	@Test
	public void testScroller()
	{
		final Scroller initialScroller = lvm.getScroller();
		assertNotNull("Expected scroller", initialScroller);
		assertSame("Wrong Scroller used by AutoScroller", lvm.getScroller(),
		        lvm.getAutoScroller().getScroller());

		final PlainScroller scroller1 = new PlainScroller();
		lvm.setScroller(scroller1);
		assertSame("Unexpected scroller", scroller1, lvm.getScroller());
		assertSame("Wrong Scroller used by AutoScroller", lvm.getScroller(),
		        lvm.getAutoScroller().getScroller());

		final SmoothScroller scroller2 = new SmoothScroller(true);
		lvm.setScroller(scroller2);
		assertSame("Unexpected scroller", scroller2, lvm.getScroller());
		assertSame("Wrong Scroller used by AutoScroller", lvm.getScroller(),
		        lvm.getAutoScroller().getScroller());

		lvm.setScroller(null);
		assertNull("Expected no scroller", lvm.getScroller());
		assertSame("Wrong Scroller used by AutoScroller", lvm.getScroller(),
		        lvm.getAutoScroller().getScroller());

		assertEquals("Unexpected number of events", 3,
		        testListener.getEventCount(ViewModel.SCROLLER_PROPERTY));
	}

	@Test
	public void testAutoScroller()
	{
		final AutoScroller autoScroller = lvm.getAutoScroller();
		assertNotNull("Missing auto-scroller", autoScroller);
		assertSame("Scroller doesn't match", lvm.getScroller(),
		        autoScroller.getScroller());

		lvm.unregister(this);
		assertNull("Auto-scroller not disposed", lvm.getScroller());
	}

	@Test
	public void testPlaylist()
	{
		assertTrue("Expected empty playlist", lvm.getPlaylist().isEmpty());

		final Playlist global = new Playlist();
		PlaylistManager.setPlaylist(global);
		assertSame("Expected global playlist", global, lvm.getPlaylist());

		lvm.setPlaylist(null);
		assertNull("Expected no playlist", lvm.getPlaylist());
	}

	@Test
	public void testControlsVisible()
	{
		assertFalse("Controls should initially be invisible",
		        lvm.isControlsVisible());

		lvm.setControlsVisible(true);
		assertTrue("Unexpected visibility", lvm.isControlsVisible());

		assertEquals("Unexpected number of events", 1,
		        testListener.getEventCount(ViewModel.CONTROLS_VISIBLE_PROPERTY));
	}

	@Test
	public void testContentVisible()
	{
		final PlaylistItem item = new PlaylistItem("test");
		lvm.getPlaylist().add(item);

		assertTrue("Content should initially be visible",
		        lvm.isContentVisible());

		lvm.setContentVisible(false);
		assertFalse("Unexpected visibility", lvm.isContentVisible());

		lvm.getPlaylist().getSelectionModel().setSelectedValue(item);
		assertTrue("Content should be made visible automatically",
		        lvm.isContentVisible());

		lvm.setBackgroundVisible(false);
		assertFalse("Content should be made invisible automatically",
		        lvm.isContentVisible());

		assertEquals("Unexpected number of events", 3,
		        testListener.getEventCount(ViewModel.CONTENT_VISIBLE_PROPERTY));
	}

	@Test
	public void testBackgroundVisible()
	{
		final PlaylistItem item = new PlaylistItem("test");
		lvm.getPlaylist().add(item);

		assertTrue("Background should initially be visible",
		        lvm.isBackgroundVisible());

		lvm.setBackgroundVisible(false);
		assertFalse("Unexpected visibility", lvm.isBackgroundVisible());

		lvm.setContentVisible(true);
		assertTrue("Background should be made visible automatically",
		        lvm.isBackgroundVisible());

		assertEquals(
		        "Unexpected number of events",
		        2,
		        testListener.getEventCount(ViewModel.BACKGROUND_VISIBLE_PROPERTY));
	}

	@Test
	public void testContent()
	{
		assertNull("Expected no content", lvm.getContent());

		lvm.setContent("test");
		assertSame("Unexpected content", "test", lvm.getContent());

		lvm.setContent(new PlaylistItem("wrappedTest"));
		assertSame("Unexpected content", "wrappedTest", lvm.getContent());

		lvm.setContent(null);
		assertNull("Expected no content", lvm.getContent());

		assertEquals("Unexpected number of events", 3,
		        testListener.getEventCount(ViewModel.CONTENT_PROPERTY));
	}
}
