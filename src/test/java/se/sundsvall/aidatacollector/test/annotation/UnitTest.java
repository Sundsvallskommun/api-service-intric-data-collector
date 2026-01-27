package se.sundsvall.aidatacollector.test.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.test.context.ActiveProfiles;

@Inherited
@Retention(RUNTIME)
@Target(TYPE)
@ActiveProfiles(UnitTest.PROFILE_NAME)
public @interface UnitTest {

	String PROFILE_NAME = "junit";
}
