package org.agmip.translators.dssat;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResourceTest {
    @Test
    public void test() {
        URL resource = this.getClass().getResource("/test.xml");
        File file = new File(resource.getFile());
        assertTrue(file.exists());
    }
}
