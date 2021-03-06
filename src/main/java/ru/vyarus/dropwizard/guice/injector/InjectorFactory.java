package ru.vyarus.dropwizard.guice.injector;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * Creates guice injector instance. Custom implementation may be required for some guice extensions, like governator.
 *
 * @author Nicholas Pace
 * @since Dec 26, 2014
 */
public interface InjectorFactory {

    /**
     * Creates an injector instance.
     *
     * @param stage   target injector stage.
     * @param modules modules supplied to injector
     * @return injector instance.
     */
    Injector createInjector(Stage stage, Iterable<? extends Module> modules);
}
