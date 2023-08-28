package org.jugnicaragua.greeting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring DI demo:
 * - Crete greeting service in English.
 *     - Create service.
 *     - Create controller.
 * - Add Interface and rename default implementation.
 * - Add Spanish implementation. The service doesn't start up (this is an expected error).
 * - Show how to inject each implementation by name.
 * - Show how to inject each implementation by type.
 * - Mark a default implementation.
 *     - How to inject the non-default implementation. Solution: using @Qualifier.
 * - Using @Qualifier.
 *     - Rename component classname.
 *     - Customize component name using @Qualifier. Change name several times and see what happen.
 *     - Custom qualifier.
 * - Injecting all implementations. Create API to display greetings in all languages.
 * - Say good morning based on passed language.
 * - Scopes
 *     - Singleton
 *     - Prototype
 *     - Request (Web Application only)
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

interface GreetingService {
	String goodMorning();

	String lang();
}

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@interface English {
}

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@interface Spanish {
}

enum LanguageType {
	EN, ES, IT
}

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@interface Lang {
	LanguageType type();
}

@Service
@Lang(type = LanguageType.EN)
class EnglishGreetingService implements GreetingService {

	@Override
	public String goodMorning() {
		return "Good morning!";
	}

	@Override
	public String lang() {
		return "EN";
	}
}

@Service
@Lang(type = LanguageType.ES)
class SpanishGreetingService implements GreetingService {

	@Override
	public String goodMorning() {
		return "Buenos dias!";
	}

	@Override
	public String lang() {
		return "ES";
	}
}

@Service
@Lang(type = LanguageType.IT)
class ItalianGreetingService implements GreetingService {

	@Override
	public String goodMorning() {
		return "Buongiorno!";
	}

	@Override
	public String lang() {
		return "IT";
	}
}

record ServiceResponse<T>(Long timestamp, T data) {
}

@RestController
@RequestMapping("/api/greeting")
class GreetingController {

	@Autowired
	@Lang(type = LanguageType.EN)
	private GreetingService greetingService;

	@Autowired
	@Qualifier("spanishGreetingService")
	private GreetingService greetingService2;

	@Autowired
	private List<GreetingService> services;

	@GetMapping("/goodMorning")
	public String goodMorning() {
		StringJoiner joiner = new StringJoiner("; ");

		return joiner.add(greetingService.goodMorning())
				.add(greetingService2.goodMorning())
				.toString();
	}

	@GetMapping("/goodMorning/{lang}")
	public String nativeGoodMorning(@PathVariable String lang) {
		Optional<GreetingService> serviceOptional = services.stream()
				.filter(service -> service.lang().equalsIgnoreCase(lang))
				.findFirst();

		return serviceOptional
				.orElseGet(() -> greetingService)
				.goodMorning();
	}

	@GetMapping("/goodMorning/all")
	public ServiceResponse<List<String>> all() {
		List<String> greetings = services.stream()
				.map(GreetingService::goodMorning)
				.toList();

		return new ServiceResponse<>(System.currentTimeMillis(), greetings);
	}
}
