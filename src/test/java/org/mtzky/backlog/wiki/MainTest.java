package org.mtzky.backlog.wiki;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void rename() throws Throwable {
        Rename.main();
    }

    @Test
    @Disabled
    void checkDeadLink() {
        CheckDeadLink.main();
    }

}