package se.sundsvall.intricdatacollector.test.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Inherited
@Retention(RUNTIME)
@Target(TYPE)
@Transactional
@ActiveProfiles(IntegrationTest.PROFILE_NAME)
public @interface IntegrationTest {

	String PROFILE_NAME = "it";
}
