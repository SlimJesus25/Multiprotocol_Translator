package eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase;

import org.apache.kafka.common.utils.Java;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaRepositoryTest {

    JavaRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JavaRepository();
    }

    @Test
    void newMessagesAreRegisteredAndSizeIsUpdated() {
        assertEquals(0,repository.getSize());
        repository.registerNewMessage("teste");
        assertEquals(1,repository.getSize());
    }

    @Test
    void messageExists() {
        repository.registerNewMessage("teste");
        assertTrue(repository.messageExists("teste"));
        assertFalse(repository.messageExists("no"));
    }
}