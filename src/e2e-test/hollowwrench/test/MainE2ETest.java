package hollowwrench.test;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.getProperty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;

/**
 * Test the application.
 **/
public final class MainE2ETest extends TestCase {
    /**
     * The app process.
     **/
    private Process process;

    /**
     * For reading from the app.
     **/
    private BufferedReader reader;

    /**
     * For writing to the app.
     **/
    private PrintStream writer;

    /**
     * A callable for reading.
     *
     * @return a callable for reading
     **/
    private Callable<? extends String> readCallable() {
        return new Callable<String>() {
            /**
             * Reads from the application.
             *
             * @return a line from the app
             **/
            @Override
            public String call() throws IOException {
                return reader.readLine();
            }
        };
    }

    /**
     * Reads from the application.
     *
     * If there is nothing to read, throw an error after 1 second.
     *
     * @return a line from the application
     * @throws Exception if something bad happens; or if there is a timeout
     **/
    private String read() throws Exception {
        return
            new SimpleTimeLimiter()
            .callWithTimeout(readCallable(), 1, SECONDS, false);
    }

    /**
     * Writes something to the application.
     *
     * @param value something
     **/
    private void write(final String value) {
        writer.println(value);
        writer.flush();
    }

    /**
     * Sets up the test.
     *
     * @throws IOException never
     **/
    @Before
    public void setup() throws IOException {
        final String[] command = {
            getProperty("e2e.java"),
            "-classpath",
            getProperty("e2e.path"),
            "-Dnet.sourceforge.cobertura.datafile="
            + getProperty("net.sourceforge.cobertura.datafile"),
            "hollowwrench.CoveredMain"
        };
        process = getRuntime().exec(command);
        reader =
            new BufferedReader(new InputStreamReader(process.getInputStream()));
        writer = new PrintStream(process.getOutputStream());
    }

    /**
     * Waits for ready.
     *
     * TODO
     *
     * There must be something wrong with the junit setp.
     * It should not be necessary to use a try/catch
     * to verify that an exception has been thrown.
     *
     * @throws Exception never
     **/
    @Test(expected = UncheckedTimeoutException.class)
    public void test_waiting() throws Exception {
        setup();
        try {
            read();
            fail();
        } catch (final UncheckedTimeoutException cause) {
            write("yes");
            process.waitFor();
            assertEquals(0, process.exitValue());
        } catch (final Throwable cause) {
            fail();
        }
    }

    /**
     * Waits for ready even if the user types in a bunch
     * of garbage.
     *
     * TODO
     *
     * There must be something wrong with the junit setp.
     * It should not be necessary to use a try/catch
     * to verify that an exception has been thrown.
     *
     * @throws Exception never
     **/
    @Test(expected = UncheckedTimeoutException.class)
    public void test_ignores_garbage() throws Exception {
        setup();
        write("lots");
        write("of");
        write("garbage");
        try {
            read();
            fail();
        } catch (final UncheckedTimeoutException cause) {
            write("yes");
            process.waitFor();
            assertEquals(0, process.exitValue());
        } catch (final Throwable cause) {
            fail();
        }
    }

    /**
     * Tests 0.
     *
     * @throws Exception never
     **/
    @Test
    public void test_0() throws Exception {
        setup();
        write("lots");
        write("of");
        write("garbage");
        write("ready");
        write("more");
        write("garbage");
        write("ready");
        assertEquals("Is the number 0?", read());
        write("yes");
        process.waitFor();
        assertEquals(0, process.exitValue());
    }

    /**
     * Test a positive number.
     *
     * @throws Exception never
     **/
    @Test
    public void test_positive_7_5() throws Exception {
        setup();
        write("lots");
        write("of");
        write("garbage");
        write("ready");
        assertEquals("Is the number 0?", read());
        write("more");
        assertEquals("Is the number 0?", read());
        write("garbage");
        assertEquals("Is the number 0?", read());
        write("higher");
        assertEquals("Is the number 1?", read());
        write("higher");
        assertEquals("Is the number 10?", read());
        write("lower");
        assertEquals("Is the number 5?", read());
        write("higher");
        assertEquals("Is the number 7?", read());
        write("higher");
        assertEquals("Is the number 8?", read());
        write("lower");
        assertEquals("Is the number 7.5?", read());
        write("yes");
        process.waitFor();
        assertEquals(0, process.exitValue());
    }

    /**
     * Test a negative number.
     *
     * @throws Exception never
     **/
    @Test
    public void test_minus_10_902() throws Exception {
        setup();
        write("lots");
        write("of");
        write("garbage");
        write("ready");
        assertEquals("Is the number 0?", read());
        write("more");
        assertEquals("Is the number 0?", read());
        write("garbage");
        assertEquals("Is the number 0?", read());
        write("lower");
        assertEquals("Is the number -1?", read());
        write("lower");
        assertEquals("Is the number -10?", read());
        write("lower");
        assertEquals("Is the number -100?", read());
        write("higher");
        assertEquals("Is the number -50?", read());
        write("higher");
        assertEquals("Is the number -30?", read());
        write("higher");
        assertEquals("Is the number -20?", read());
        write("higher");
        assertEquals("Is the number -15?", read());
        write("higher");
        assertEquals("Is the number -12?", read());
        write("higher");
        assertEquals("Is the number -11?", read());
        write("higher");
        assertEquals("Is the number -10.5?", read());
        write("lower");
        assertEquals("Is the number -10.7?", read());
        write("lower");
        assertEquals("Is the number -10.8?", read());
        write("lower");
        assertEquals("Is the number -10.9?", read());
        write("lower");
        assertEquals("Is the number -10.95?", read());
        write("higher");
        assertEquals("Is the number -10.92?", read());
        write("higher");
        assertEquals("Is the number -10.91?", read());
        write("higher");
        assertEquals("Is the number -10.905?", read());
        write("higher");
        assertEquals("Is the number -10.902?", read());
        write("yes");
        process.waitFor();
        assertEquals(0, process.exitValue());
    }
}
